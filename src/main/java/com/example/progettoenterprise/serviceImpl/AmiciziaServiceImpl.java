package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Amicizia;
import com.example.progettoenterprise.data.entities.Amicizia.StatoAmicizia;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.AmiciziaRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.service.AmiciziaService;
import com.example.progettoenterprise.dto.AmiciziaDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmiciziaServiceImpl implements AmiciziaService {
    private final AmiciziaRepository amiciziaRepository;
    private final UtenteRepository utenteRepository;
    private final MessageLang messageLang;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AmiciziaDTO inviaRichiesta(Long richiedenteId, Long riceventeId) {
        log.info("Tentativo di invio richiesta amicizia: da ID {} a ID {}", richiedenteId, riceventeId);

        if (richiedenteId.equals(riceventeId)) {
            log.error("L'utente {} ha provato a chiedere l'amicizia a se stesso", richiedenteId);
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.self_request"));
        }

        Utente richiedente = utenteRepository.findById(richiedenteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", richiedenteId)));

        Utente ricevente = utenteRepository.findById(riceventeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", riceventeId)));

        amiciziaRepository.findQualsiasiRelazione(richiedente, ricevente)
                .ifPresent(a -> {
                    log.warn("Richiesta fallita: esiste già una relazione tra {} e {}", richiedente.getUsername(), ricevente.getUsername());
                    throw new IllegalStateException(messageLang.getMessage("amicizia.already_exists"));
                });

        Amicizia nuovaAmicizia = new Amicizia();
        nuovaAmicizia.setRichiedente(richiedente);
        nuovaAmicizia.setRicevente(ricevente);
        nuovaAmicizia.setStato(StatoAmicizia.IN_ATTESA);

        Amicizia salvata = amiciziaRepository.save(nuovaAmicizia);
        log.info("Richiesta di amicizia creata con successo (ID: {}) tra {} e {}",
                salvata.getId(), richiedente.getUsername(), ricevente.getUsername());

        return modelMapper.map(salvata, AmiciziaDTO.class);
    }

    @Override
    @Transactional
    public AmiciziaDTO accettaRichiesta(Long amiciziaId, Long riceventeId) {
        log.info("L'utente ID {} sta tentando di accettare la richiesta ID {}", riceventeId, amiciziaId);

        Amicizia amicizia = amiciziaRepository.findById(amiciziaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("amicizia.notexist", amiciziaId)));

        if (!amicizia.getRicevente().getId().equals(riceventeId)) {
            log.error("Accesso non autorizzato: l'utente {} ha provato ad accettare la richiesta {} destinata a {}",
                    riceventeId, amiciziaId, amicizia.getRicevente().getId());
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.unauthorized"));
        }

        if (amicizia.getStato() != StatoAmicizia.IN_ATTESA) {
            log.warn("La richiesta {} non è più in stato pendente (stato attuale: {})", amiciziaId, amicizia.getStato());
            throw new IllegalStateException(messageLang.getMessage("amicizia.not_pending"));
        }

        amicizia.setStato(StatoAmicizia.ACCETTATA);
        Amicizia aggiornata = amiciziaRepository.save(amicizia);

        log.info("Amicizia ID {} accettata. Ora {} e {} sono amici",
                amiciziaId, amicizia.getRichiedente().getUsername(), amicizia.getRicevente().getUsername());

        return modelMapper.map(aggiornata, AmiciziaDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmiciziaDTO> getMieiAmici(Long utenteId) {
        log.info("Recupero lista amici confermati per l'utente ID: {}", utenteId);

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", utenteId)));

        return amiciziaRepository.findAllAmiciConfermati(utente)
                .stream()
                .map(a -> modelMapper.map(a, AmiciziaDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmiciziaDTO> getRichiesteRicevute(Long utenteId) {
        log.info("Recupero richieste di amicizia ricevute per l'utente ID: {}", utenteId);

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", utenteId)));

        return amiciziaRepository.findByRiceventeAndStato(utente, StatoAmicizia.IN_ATTESA)
                .stream()
                .map(a -> modelMapper.map(a, AmiciziaDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmiciziaDTO> getRichiesteInviate(Long utenteId) {
        log.info("Recupero richieste di amicizia inviate dall'utente ID: {}", utenteId);

        Utente utente = utenteRepository.findById(utenteId).orElseThrow(
                () -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId))
        );
        return amiciziaRepository.findByRichiedenteAndStato(utente, StatoAmicizia.IN_ATTESA)
                .stream()
                .map(a -> modelMapper.map(a, AmiciziaDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void rifiutaRichiesta(Long amiciziaId, Long riceventeId) {
        log.warn("L'utente ID {} sta rifiutando la richiesta di amicizia ID {}", riceventeId, amiciziaId);

        Amicizia amicizia = amiciziaRepository.findById(amiciziaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("amicizia.notexist", amiciziaId)));

        if (!amicizia.getRicevente().getId().equals(riceventeId)) {
            log.error("Rifiuto fallito: l'utente {} non è il destinatario della richiesta {}", riceventeId, amiciziaId);
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.unauthorized"));
        }

        if (amicizia.getStato() != StatoAmicizia.IN_ATTESA) {
            throw new IllegalStateException(messageLang.getMessage("amicizia.not_pending"));
        }

        amicizia.setStato(StatoAmicizia.RIFIUTATA);
        amiciziaRepository.save(amicizia);
        log.info("Richiesta {} rifiutata correttamente", amiciziaId);
    }

    @Override
    @Transactional
    public void rimuoviAmico(Long richiedenteId, Long riceventeId) {
        log.warn("Tentativo di rimozione amicizia tra ID {} e ID {}", richiedenteId, riceventeId);

        Utente richiedente = utenteRepository.findById(richiedenteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", richiedenteId)));
        Utente ricevente = utenteRepository.findById(riceventeId).orElseThrow(
                () -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", riceventeId))
        );

        Amicizia esistente = amiciziaRepository.findQualsiasiRelazione(richiedente, ricevente)
                .orElseThrow(() -> {
                    log.error("Impossibile rimuovere: nessuna relazione trovata tra {} e {}", richiedenteId, riceventeId);
                    return new EntityNotFoundException(messageLang.getMessage("amicizia.notexist", richiedenteId, riceventeId));
                });

        if (esistente.getStato() != StatoAmicizia.ACCETTATA) {
            log.warn("Tentativo di rimuovere un'amicizia non confermata (ID: {})", esistente.getId());
            throw new IllegalStateException(messageLang.getMessage("amicizia.not_accepted"));
        }

        amiciziaRepository.delete(esistente);
        log.info("Relazione di amicizia ID {} eliminata con successo tra {} e {}",
                esistente.getId(), richiedente.getUsername(), ricevente.getUsername());
    }
}