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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrenotazioneServiceImpl implements PrenotazioneService {

    // Dimensioni della pagina per la ricerca
    private static final int SIZE_FOR_PAGE = 10;

    private final ViaggioRepository viaggioRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    private String formattaDataIcs(LocalDate data) {
        if (data == null) return "";
        return data.atStartOfDay().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    }

    @Override
    @Transactional
    public PrenotazioneDTO creaPrenotazione(Long idViaggio, Long idUtente, Integer numeroPersone) {
        Viaggio viaggio = viaggioRepository.findById(idViaggio)
                .orElseThrow(() -> {
                    log.error("Impossibile creare la prenotazione: viaggio ID {} non trovato", idViaggio);
                    return new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", idViaggio));
                });

        // Controlla che la data di inizio del viaggio sia nel futuro
        if (viaggio.getDataInizio().isBefore(LocalDate.now())) {
            log.warn("Tentativo di prenotazione per un viaggio già iniziato/concluso: ID {}", idViaggio);
            throw new IllegalStateException(messageLang.getMessage("prenotazione.viaggio.scaduto"));
        }


        List<Prenotazione> prenotazioniSovrapposte = prenotazioneRepository.findPrenotazioniSovrapposte(
                idUtente, viaggio.getDataInizio(), viaggio.getDataFine());

        if (!prenotazioniSovrapposte.isEmpty()) {
            log.warn("Prenotazione bloccata: l'utente {} ha già un viaggio in quelle date", idUtente);
            throw new IllegalStateException("Non puoi prenotare questo viaggio perché le date si sovrappongono con un'altra tua prenotazione.");
        }

        if (viaggio.getPartecipantiAttuali() + numeroPersone > viaggio.getMaxPartecipanti()) {
            log.warn("Tentativo di prenotazione fallito per posti esauriti: Viaggio ID {}", idViaggio);
            throw new IllegalStateException("Non ci sono abbastanza posti disponibili per questo viaggio.");
        }

        viaggio.setPartecipantiAttuali(viaggio.getPartecipantiAttuali() + numeroPersone);
        viaggioRepository.save(viaggio);
        Utente utenteRichiedente = utenteRepository.findById(idUtente)
                .orElseThrow(() -> {
                    log.error("Impossibile creare la prenotazione: utente ID {} non trovato", idUtente);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", idUtente));
                });

        Prenotazione nuovaPrenotazione = new Prenotazione();
        nuovaPrenotazione.setViaggio(viaggio);
        nuovaPrenotazione.setViaggiatore(utenteRichiedente);
        nuovaPrenotazione.setNumeroPersone(numeroPersone);
        nuovaPrenotazione.setStato(Prenotazione.StatoPrenotazione.IN_ATTESA); // Stato iniziale standard

        Prenotazione prenotazioneSalvata = prenotazioneRepository.save(nuovaPrenotazione);
        return modelMapper.map(prenotazioneSalvata, PrenotazioneDTO.class);
    }

    @Override
    @Transactional
    public void cancellaPrenotazione(Long idPrenotazione, Long idUtente) {
        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> {
                    log.error("Impossibile cancellare la prenotazione: prenotazione ID {} non trovata", idPrenotazione);
                    return new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", idPrenotazione));
                });

        if (!prenotazione.getViaggiatore().getId().equals(idUtente)) {
            log.error("Accesso non autorizzato: l'utente {} ha provato a cancellare la prenotazione ID {}, appartenente all'utente {}",
                    idUtente, idPrenotazione, prenotazione.getViaggiatore().getId());
            throw new IllegalArgumentException(messageLang.getMessage("prenotazione.unauthorized"));
        }
        if (prenotazione.getStato() == Prenotazione.StatoPrenotazione.CONFERMATA) {
            log.warn("Tentativo di cancellazione bloccato: la prenotazione ID {} è già confermata", idPrenotazione);
            throw new IllegalStateException("Non puoi annullare una prenotazione già confermata. Contatta l'organizzatore.");
        }
        Viaggio viaggio = prenotazione.getViaggio();
        viaggio.setPartecipantiAttuali(viaggio.getPartecipantiAttuali() - prenotazione.getNumeroPersone());
        viaggioRepository.save(viaggio);


        prenotazioneRepository.delete(prenotazione);
    }

    @Override
    public PrenotazioneDTO getPrenotazioneById(Long id, Long utenteId) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Impossibile recuperare la prenotazione: prenotazione ID {} non trovata", id);
                    return new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", id));
                });

        Utente utenteLoggato = utenteRepository.findById(utenteId)
                .orElseThrow(() -> {
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
        java.time.LocalDateTime dataInizio = viaggio.getDataInizio().atStartOfDay();
        java.time.LocalDateTime dataFine = viaggio.getDataFine().atTime(java.time.LocalTime.MAX);

        // formatto per lo stringBuilder
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String dataInizioStr = dataInizio.format(formatter);
        String dataFineStr = dataFine.format(formatter);
        String dataCreazioneStr = java.time.LocalDateTime.now().format(formatter);

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\n")
                .append("VERSION:2.0\n")
                .append("PRODID:-//Progetto Enterprise//Calendario Viaggi//IT\n")
                .append("CALSCALE:GREGORIAN\n")
                .append("BEGIN:VEVENT\n")
                .append("UID:viaggio-").append(viaggio.getId()).append("@progettoenterprise.com\n")
                .append("DTSTAMP:").append(dataCreazioneStr).append("\n")
                .append("DTSTART:").append(dataInizioStr).append("\n")
                .append("DTEND:").append(dataFineStr).append("\n")
                .append("SUMMARY:Viaggio: ").append(viaggio.getTitolo()).append("\n")
                .append("DESCRIPTION:Promemoria per il tuo viaggio a ").append(viaggio.getDestinazione()).append("\n")
                .append("LOCATION:").append(viaggio.getDestinazione()).append("\n")
                .append("END:VEVENT\n")
                .append("END:VCALENDAR");

        return ics.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrenotazioneDTO> ricercaFiltrata(PrenotazioneSpecification.PrenotazioneFilter prenotazioneFilter, Long utenteId, int page) {
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
        else {
            log.debug("Ruolo ADMIN rilevato: forzatura filtro su tutte le prenotazioni");
        }

        // Paginazione della ricerca
        PageRequest pageRequest = PageRequest.of(page, SIZE_FOR_PAGE, Sort.by("id").descending());
        Page<Prenotazione> prenotazionePage = prenotazioneRepository.findAll(PrenotazioneSpecification.withFilter(prenotazioneFilter), pageRequest);

        // Controllo sulla pagina corrente
        if ((page < 0 || page >= prenotazionePage.getTotalPages()) && prenotazionePage.getTotalPages() > 0) {
            log.warn("Pagina non valida: {}. Pagina totale: {}", page, prenotazionePage.getTotalPages());
            throw new IllegalArgumentException(messageLang.getMessage("prenotazione.invalid_page"));
        }

        return prenotazionePage.map(p -> modelMapper.map(p, PrenotazioneDTO.class));
    }

    // Incolla questo metodo all'interno di PrenotazioneServiceImpl.java
    @Override
    @Transactional(readOnly = true)
    public Optional<PrenotazioneDTO> ottieniStatoPrenotazioneUtente(Long viaggioId, Long utenteId) {
        log.debug("Verifica prenotazione utente ID {} per il viaggio ID {}", utenteId, viaggioId);
        return prenotazioneRepository.findByViaggioIdAndViaggiatoreId(viaggioId, utenteId)
                .map(prenotazione -> modelMapper.map(prenotazione, PrenotazioneDTO.class));
    }
}