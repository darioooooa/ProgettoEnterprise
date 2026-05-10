package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.PrenotazioneSpecification;
import com.example.progettoenterprise.data.service.PrenotazioneService;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrenotazioneServiceImpl implements PrenotazioneService {
    private final ViaggioRepository viaggioRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    private String formattaDataIcs(LocalDateTime data) {
        if (data == null) return "";


        return data.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    }


    @Override
    @Transactional
    public PrenotazioneDTO creaPrenotazione(Long idViaggio, Long idUtente, Integer numeroPersone) {
        Viaggio viaggio= viaggioRepository.findById(idViaggio)
                .orElseThrow(() -> {
                    log.error("Impossibile creare la prenotazione: viaggio ID {} non trovato", idViaggio);
                    return new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", idViaggio));
                });

        // Controlla che la data di inizio del viaggio sia nel futuro
        if (viaggio.getDataInizio().isBefore(LocalDateTime.now())) {
            log.warn("Tentativo di prenotazione per un viaggio già iniziato/concluso: ID {}", idViaggio);
            throw new IllegalStateException(messageLang.getMessage("prenotazione.viaggio.scaduto"));
        }

        // TODO: vedere parte relativa ai posti massimi del viaggio

        Utente utenteRichiedente = utenteRepository.findById(idUtente)
                .orElseThrow(() -> {
                    log.error("Impossibile creare la prenotazione: utente ID {} non trovato", idUtente);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", idUtente));
                });
        Prenotazione nuovaPrenotazione = new Prenotazione();
        nuovaPrenotazione.setViaggio(viaggio);
        nuovaPrenotazione.setViaggiatore(utenteRichiedente);
        nuovaPrenotazione.setNumeroPersone(numeroPersone);
        nuovaPrenotazione.setDataPrenotazione(LocalDateTime.now());
        nuovaPrenotazione.setStato(Prenotazione.StatoPrenotazione.IN_ATTESA); // Stato iniziale standard

        Prenotazione prenotazioneSalvata = prenotazioneRepository.save(nuovaPrenotazione);
        return modelMapper.map(prenotazioneSalvata, PrenotazioneDTO.class);
    }

    @Override
    @Transactional
    public void cancellaPrenotazione(Long idPrenotazione, Long idUtente) {
            Prenotazione prenotazione= prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> {
                    log.error("Impossibile cancellare la prenotazione: prenotazione ID {} non trovata", idPrenotazione);
                    return new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", idPrenotazione));
                });
            if(!prenotazione.getViaggiatore().getId().equals(idUtente)){
                log.error("Accesso non autorizzato: l'utente {} ha provato a cancellare la prenotazione ID {}, appartenente all'utente {}",
                        idUtente, idPrenotazione, prenotazione.getViaggiatore().getId());
                throw new IllegalArgumentException(messageLang.getMessage("prenotazione.unauthorized"));
            }
            prenotazioneRepository.delete(prenotazione);


    }

    @Override
    public PrenotazioneDTO getPrenotazioneById(Long id, Long utenteId) {
       Prenotazione prenotazione= prenotazioneRepository.findById(id)
               .orElseThrow(() -> {
                   log.warn("Impossibile recuperare la prenotazione: prenotazione ID {} non trovata", id);
                   return new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", id));
               });
       Utente utenteLoggato = utenteRepository.findById(utenteId)
               .orElseThrow( () -> {
                   log.error("Impossibile recuperare la prenotazione: utente ID {} non trovato", utenteId);
                   return new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId));
               });

       boolean isAdmin = utenteLoggato.getRuolo().equals(Utente.Ruolo.ROLE_ADMIN);
       boolean isOrganizzatoreViaggio = prenotazione.getViaggio().getOrganizzatore().getId().equals(utenteId);
       boolean isProprietarioPrenotazione = prenotazione.getViaggiatore().getId().equals(utenteId);

       if (!isAdmin && !isOrganizzatoreViaggio && !isProprietarioPrenotazione) {
           log.error("Accesso non autorizzato: l'utente {} ha provato a recuperare la prenotazione ID {}", utenteId, id);
           throw new IllegalArgumentException(messageLang.getMessage("prenotazione.unauthorized"));
       }

       return modelMapper.map(prenotazione, PrenotazioneDTO.class);


    }

    @Override
    public byte[] esportaPrenotazioni(Long idPrenotazione) {
        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> {
                    log.error("Impossibile esportare le prenotazioni: prenotazione ID {} non trovata", idPrenotazione);
                    return new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", idPrenotazione));
                });

        Viaggio viaggio = prenotazione.getViaggio();

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\n")
                .append("VERSION:2.0\n")
                .append("PRODID:-//ProgettoEnterprise//GestioneViaggi//IT\n")
                .append("BEGIN:VEVENT\n")
                .append("UID:").append(prenotazione.getId()).append("@gestione-viaggi.it\n")
                .append("SUMMARY:").append(viaggio.getTitolo()).append("\n")
                .append("DESCRIPTION:Prenotazione per ").append(prenotazione.getNumeroPersone()).append(" persone\n")

                // Ora passiamo LocalDateTime, e il metodo lo gestirà correttamente
                .append("DTSTART:").append(formattaDataIcs(viaggio.getDataInizio())).append("\n")
                .append("DTEND:").append(formattaDataIcs(viaggio.getDataFine())).append("\n")

                .append("END:VEVENT\n")
                .append("END:VCALENDAR");

        return ics.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrenotazioneDTO> ricercaFiltrata(PrenotazioneSpecification.PrenotazioneFilter prenotazioneFilter, Long utenteId) {

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> {
                    log.error("Impossibile recuperare le prenotazioni: utente ID {} non trovato", utenteId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId));
                });

        // Se è un viaggiatore, si forza il filtro per l'utente specificato
        if (utente.getRuolo().equals(Utente.Ruolo.ROLE_VIAGGIATORE)) {
            log.debug("Ruolo VIAGGIATORE rilevato: forzatura filtro su proprie prenotazioni (ID {})", utenteId);
            prenotazioneFilter.setViaggiatoreId(utenteId);
        }
        // Se è un organizzatore, si forza il filtro per solo i suoi viaggi
        else if (utente.getRuolo().equals(Utente.Ruolo.ROLE_ORGANIZZATORE)) {
            log.debug("Ruolo ORGANIZZATORE rilevato: forzatura filtro su propri viaggi (ID {})", utenteId);
            prenotazioneFilter.setOrganizzatoreProprietarioId(utente.getId());
        }
        // Se è l'admin, può vedere tutto
        else{
            log.debug("Ruolo ADMIN rilevato: forzatura filtro su tutte le prenotazioni");
        }

        List<Prenotazione> prenotazioni = prenotazioneRepository.findAll(PrenotazioneSpecification.withFilter(prenotazioneFilter));
        return prenotazioni.stream()
                .map(p -> modelMapper.map(p, PrenotazioneDTO.class))
                .collect(Collectors.toList());
    }


}
