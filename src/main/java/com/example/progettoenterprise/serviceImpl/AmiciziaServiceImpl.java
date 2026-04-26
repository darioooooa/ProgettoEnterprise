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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmiciziaServiceImpl implements AmiciziaService {
    private final AmiciziaRepository amiciziaRepository;
    private final UtenteRepository utenteRepository;
    private final MessageLang messageLang;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AmiciziaDTO inviaRichiesta(Long richiedenteId, Long riceventeId) {
        // Controllo logico: non puoi essere amico di te stesso
        if (richiedenteId.equals(riceventeId)) {
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.self_request"));
        }
        Utente richiedente = utenteRepository.findById(richiedenteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", richiedenteId)));

        Utente ricevente = utenteRepository.findById(riceventeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", riceventeId)));

        // Controllo se esiste già una relazione (qualsiasi sia lo stato)
        amiciziaRepository.findQualsiasiRelazione(richiedente, ricevente)
                .ifPresent(a -> {
                    throw new IllegalStateException(messageLang.getMessage("amicizia.already_exists"));
                });
        Amicizia nuovaAmicizia = new Amicizia();
        nuovaAmicizia.setRichiedente(richiedente);
        nuovaAmicizia.setRicevente(ricevente);
        nuovaAmicizia.setStato(StatoAmicizia.IN_ATTESA);
        nuovaAmicizia.setDataRichiesta(LocalDateTime.now());

        Amicizia salvata = amiciziaRepository.save(nuovaAmicizia);
        return modelMapper.map(salvata, AmiciziaDTO.class);
    }

    @Override
    @Transactional
    public AmiciziaDTO accettaRichiesta(Long amiciziaId, Long riceventeId) {
        Amicizia amicizia = amiciziaRepository.findById(amiciziaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("amicizia.notexist", amiciziaId)));

        // Sicurezza: solo il ricevente reale può accettare
        if (!amicizia.getRicevente().getId().equals(riceventeId)) {
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.unauthorized"));
        }

        if (amicizia.getStato() != StatoAmicizia.IN_ATTESA) {
            throw new IllegalStateException(messageLang.getMessage("amicizia.not_pending"));
        }

        amicizia.setStato(StatoAmicizia.ACCETTATA);
        amicizia.setDataRisposta(LocalDateTime.now());
        Amicizia aggiornata = amiciziaRepository.save(amicizia);
        return modelMapper.map(aggiornata, AmiciziaDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmiciziaDTO> getMieiAmici(Long utenteId) {
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
        Amicizia amicizia = amiciziaRepository.findById(amiciziaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("amicizia.notexist", amiciziaId)));
        if (!amicizia.getRicevente().getId().equals(riceventeId)) {
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.unauthorized"));
        }
        if (amicizia.getStato() != StatoAmicizia.IN_ATTESA) {
            throw new IllegalStateException(messageLang.getMessage("amicizia.not_pending"));
        }
        amicizia.setStato(StatoAmicizia.RIFIUTATA);
        amiciziaRepository.save(amicizia);
    }

    @Override
    @Transactional
    public void rimuoviAmico(Long richiedenteId, Long riceventeId) {
        if (richiedenteId.equals(riceventeId)) {
            throw new IllegalArgumentException(messageLang.getMessage("amicizia.self_request"));
        }
        Utente richiedente = utenteRepository.findById(richiedenteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", richiedenteId)));
        Utente ricevente = utenteRepository.findById(riceventeId).orElseThrow(
                () -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", riceventeId))
        );
        Amicizia esistente = amiciziaRepository.findQualsiasiRelazione(richiedente, ricevente)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("amicizia.notexist", richiedenteId, riceventeId)));
        if (esistente.getStato() != StatoAmicizia.ACCETTATA) {
            throw new IllegalStateException(messageLang.getMessage("amicizia.not_accepted"));
        }
        amiciziaRepository.delete(esistente);
    }

}



