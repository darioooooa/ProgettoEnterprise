package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.data.service.PagamentoService;
import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import com.example.progettoenterprise.dto.ViaggioMappaDTO;
import com.example.progettoenterprise.events.RimborsoErogatoEvent;
import com.example.progettoenterprise.events.ViaggioConsigliatoEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViaggioServiceImpl implements ViaggioService {

    // Dimensioni della pagina per la ricerca
    private static final int SIZE_FOR_PAGE = 10;

    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;
    private final PagamentoService pagamentoService;
    private final PrenotazioneRepository prenotazioneRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailServiceImpl emailService;

    @Override
    @Transactional
    public ViaggioDTO creaViaggio(ViaggioDTO viaggioDTO, Long organizzatoreId) {
        Utente organizzatore = utenteRepository.findById(organizzatoreId)
                .orElseThrow(() -> {
                    log.error("Impossibile creare viaggio: utente ID {} non trovato", organizzatoreId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", organizzatoreId));
                });
        Viaggio viaggio = modelMapper.map(viaggioDTO, Viaggio.class);
        if (viaggioDTO.getDataInizio() != null) {
            viaggio.setDataInizio(viaggioDTO.getDataInizio());
        }
        if (viaggioDTO.getDataFine() != null) {
            viaggio.setDataFine(viaggioDTO.getDataFine());
        }
        //assegnare le tappe al viaggio
        if (viaggio.getTappe() != null) {
            viaggio.getTappe().forEach(tappa -> tappa.setViaggio(viaggio));
        }
        // Controllo sui dati del viaggio
        if (viaggio.getPrezzo() <= 0) {
            log.warn("Tentativo di creazione viaggio con prezzo non valido: {}", viaggio.getPrezzo());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_price"));
        }
        if (viaggio.getDataInizio() == null || (viaggio.getDataFine() != null && viaggio.getDataFine().isBefore(viaggio.getDataInizio()))){
            log.warn("Tentativo di creazione viaggio con date non valide. Inizio: {}, Fine: {}",
                    viaggio.getDataInizio(), viaggio.getDataFine());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_date"));
        }

        viaggio.setOrganizzatore(organizzatore);
        viaggio.setMediaRecensioni(0.0);
        viaggio.setNumeroRecensioni(0);
        viaggio.setPartecipantiAttuali(0);
        viaggio.setStato(Viaggio.StatoViaggio.APERTO);
        Viaggio salvato = viaggioRepository.save(viaggio);
        //messo nel try catch perche in caso ci sono errori con le notifiche non bloccano la
        //creazione del viaggio
        try {

            List<Utente> viaggiGiaFatti = prenotazioneRepository.findViaggiatoriViaggiGiaFatti(salvato.getDestinazione());

            for (Utente utente : viaggiGiaFatti) {
                // Escludiamo il creatore del viaggio (perché magari anche lui in passato c'era andato come passeggero)
                if (!utente.getId().equals(salvato.getOrganizzatore().getId())) {

                    String token = utente.getFirebaseToken();
                    if (token != null && !token.trim().isEmpty()) {
                        String cittaMaiuscola = salvato.getDestinazione().toUpperCase();
                        ViaggioConsigliatoEvent evento = new ViaggioConsigliatoEvent(
                                token,
                                cittaMaiuscola,
                                salvato.getTitolo()
                        );
                        eventPublisher.publishEvent(evento);
                    }
                }
            }
        } catch (Exception e) {
            log.error("⚠️ Errore non bloccante durante l'invio dei consigli: {}", e.getMessage());
        }
        return modelMapper.map(salvato, ViaggioDTO.class);
    }

    @Override
    @Transactional
    public void eliminaViaggio(Long viaggioId, Long organizzatoreId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> {
                    log.warn("Tentativo di eliminazione fallito: viaggio ID {} inesistente", viaggioId);
                    return new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId));
                });
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)) {
            log.error("Accesso negato! L'utente ID {} ha tentato di eliminare il viaggio ID {} che appartiene all'utente ID {}",
                    organizzatoreId, viaggioId, viaggio.getOrganizzatore().getId());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }
        List<Prenotazione> prenotazioniDaRimborsare = prenotazioneRepository
                .findByViaggioIdAndStato(viaggioId, Prenotazione.StatoPrenotazione.CONFERMATA);

        for (Prenotazione prenotazione : prenotazioniDaRimborsare) {
            try {
                // Calcola l'importo del rimborso prima di processarlo
                double importoRimborso = prenotazione.getNumeroPersone() * viaggio.getPrezzo();

                pagamentoService.rimborsaPrenotazione(prenotazione.getId());
                log.info("Rimborso Stripe automatico effettuato per la prenotazione ID: {}", prenotazione.getId());

                // ✅ Notifica push
                String token = prenotazione.getViaggiatore().getFirebaseToken();
                if (token != null && !token.trim().isEmpty()) {
                    RimborsoErogatoEvent evento = new RimborsoErogatoEvent(token, viaggio.getTitolo());
                    eventPublisher.publishEvent(evento);
                }

                // ✅ NUOVO: Invio email di rimborso al viaggiatore
                try {
                    String emailViaggiatore = prenotazione.getViaggiatore().getEmail();
                    String usernameViaggiatore = prenotazione.getViaggiatore().getUsername();

                    if (emailViaggiatore != null && !emailViaggiatore.trim().isEmpty()) {
                        emailService.inviaEmailRimborsoViaggioEliminato(
                                emailViaggiatore,
                                usernameViaggiatore,
                                viaggio.getTitolo(),
                                viaggio.getDestinazione(),
                                viaggio.getDataInizio(),
                                viaggio.getDataFine(),
                                importoRimborso
                        );
                        log.info("✅ Email di rimborso inviata a {} per viaggio ID {}", emailViaggiatore, viaggioId);
                    }
                } catch (Exception emailEx) {
                    // Non bloccare il flusso se l'email fallisce
                    log.error("⚠️ Errore invio email rimborso per prenotazione ID {}: {}",
                            prenotazione.getId(), emailEx.getMessage());
                }

            } catch (Exception e) {
                log.error("Rimborso Stripe fallito per la prenotazione id {} (motivo: {}). Forzo annullamento prenotazione.",
                        prenotazione.getId(), e.getMessage());

                prenotazione.setStato(Prenotazione.StatoPrenotazione.ANNULLATA);
                prenotazioneRepository.save(prenotazione);
            }
        }

        if (viaggio.getPrenotazioniRicevute() != null) {
            for (Prenotazione p : viaggio.getPrenotazioniRicevute()) {
                p.setStato(Prenotazione.StatoPrenotazione.ANNULLATA);
            }
        }

        viaggio.setStato(Viaggio.StatoViaggio.ANNULLATO);
        viaggioRepository.save(viaggio);

        log.info("Il viaggio ID {} è stato contrassegnato come ANNULLATO", viaggioId);
    }

    // Metodo per fornire dettagli delle recensioni di un viaggio
    @Override
    @Transactional(readOnly = true)
    // Prima cerca le statistiche del viaggio nella cache
    @Cacheable(value = CacheConfig.CACHE_VIAGGI_MEDIA, key = "#viaggioId")
    public Map<String, Object> getStatisticheRecensioni(Long viaggioId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", viaggioId)));

        return Map.of(
                "viaggioId", viaggio.getId(),
                "destinazione", viaggio.getDestinazione() != null ? viaggio.getDestinazione() : "",
                "mediaRecensioni", viaggio.getMediaRecensioni(),
                "numeroRecensioni", viaggio.getNumeroRecensioni(),
                "organizzatoreUsername", viaggio.getOrganizzatore() != null ? viaggio.getOrganizzatore().getUsername() : "",
                "organizzatoreId", viaggio.getOrganizzatore() != null ? viaggio.getOrganizzatore().getId() : 0L
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ViaggioDTO> ricercaFiltrata(ViaggioSpecification.ViaggioFilter viaggioFilter, Long utenteId, int page) {

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> {
                    log.error("Impossibile creare viaggio: utente ID {} non trovato", utenteId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId));
                });

        if (utente.getRuolo().equals(Utente.Ruolo.ROLE_VIAGGIATORE)) {
            viaggioFilter.setMostraSoloDisponibili(true);
        }

        // Paginazione della ricerca
        PageRequest pageRequest = PageRequest.of(page, SIZE_FOR_PAGE, Sort.by("id").descending());
        Page<Viaggio> viaggiPage = viaggioRepository.findAll(ViaggioSpecification.withFilter(viaggioFilter), pageRequest);

        // Controllo sulla pagina corrente
        if ((page < 0 || page >= viaggiPage.getTotalPages()) && viaggiPage.getTotalPages() > 0) {
            log.warn("Pagina non valida: {}. Pagina totale: {}", page, viaggiPage.getTotalPages());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_page"));
        }

        return viaggiPage.map(viaggio -> modelMapper.map(viaggio, ViaggioDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViaggioMappaDTO> getViaggiMappa(Long utenteId) {

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> {
                    log.error("Impossibile caricare mappa: utente ID {} non trovato", utenteId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId));
                });

        List<Viaggio> viaggi;

        if (utente.getRuolo().equals(Utente.Ruolo.ROLE_ORGANIZZATORE)) {
            viaggi = viaggioRepository.findByOrganizzatoreId(utenteId);
        } else {

            viaggi = viaggioRepository.findAll();
        }

        // 3. Mappiamo nel DTO
        return viaggi.stream()
                // Teniamo solo i viaggi che non sono annullati
                .filter(viaggio -> viaggio.getStato() != null && viaggio.getStato() != Viaggio.StatoViaggio.ANNULLATO)
                .map(viaggio -> {
                    ViaggioMappaDTO dto = new ViaggioMappaDTO();
                    dto.setId(viaggio.getId());
                    dto.setTitolo(viaggio.getTitolo());
                    dto.setLatitudine(viaggio.getLatitudine());
                    dto.setLongitudine(viaggio.getLongitudine());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ViaggioDTO getViaggioById(Long viaggioId, Long id) {
        Viaggio viaggio = viaggioRepository.findByIdConTappe(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", id)));


        return modelMapper.map(viaggio, ViaggioDTO.class);

    }


    @Override
    @Transactional
    public ViaggioDTO modificaViaggio(Long id, ViaggioDTO viaggioDTO, Long organizzatoreId) {
        Viaggio viaggioEsistente = viaggioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", id)));

        if (!viaggioEsistente.getOrganizzatore().getId().equals(organizzatoreId)) {
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }

        // Controlla se il viaggio è già iniziato ed è in uno stato che permette la modifica del prezzo
        if (viaggioEsistente.getDataInizio() != null && viaggioEsistente.getDataInizio().isBefore(LocalDate.now())) {
            log.warn("Tentativo di modifica fallito: Il viaggio ID {} è già iniziato il {}", id, viaggioEsistente.getDataInizio());
            throw new IllegalStateException(messageLang.getMessage("viaggio.already_started"));

        }
        if (viaggioEsistente.getStato() == Viaggio.StatoViaggio.IN_CORSO ||
                viaggioEsistente.getStato() == Viaggio.StatoViaggio.COMPLETATO) {
            log.warn("Tentativo di modifica fallito: Lo stato del viaggio ID {} è {}", id, viaggioEsistente.getStato());
            throw new IllegalStateException(messageLang.getMessage("viaggio.already_started"));
        }


        viaggioEsistente.setTitolo(viaggioDTO.getTitolo());
        viaggioEsistente.setDescrizione(viaggioDTO.getDescrizione());
        viaggioEsistente.setDestinazione(viaggioDTO.getDestinazione());
        viaggioEsistente.setCittaPartenza(viaggioDTO.getCittaPartenza ());
        viaggioEsistente.setPrezzo(viaggioDTO.getPrezzo());
        viaggioEsistente.setDataInizio(viaggioDTO.getDataInizio());
        viaggioEsistente.setDataFine(viaggioDTO.getDataFine());

        if (viaggioDTO.getTappe() != null) {
            viaggioEsistente.getTappe().clear();

            List<AttivitaViaggio> nuoveTappe = viaggioDTO.getTappe().stream()
                    .map(tappaDto -> {
                        AttivitaViaggio tappa = modelMapper.map(tappaDto, AttivitaViaggio.class);
                        tappa.setViaggio(viaggioEsistente);
                        return tappa;
                    }).collect(Collectors.toList());


            viaggioEsistente.getTappe().addAll(nuoveTappe);
        }

        Viaggio aggiornato = viaggioRepository.save(viaggioEsistente);
        return modelMapper.map(aggiornato, ViaggioDTO.class);
    }
    @Override
    @Transactional(readOnly = true)
    public ViaggioDTO getViaggioById(Long id) {
        Viaggio viaggio = viaggioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", id)));


        ViaggioDTO dto = modelMapper.map(viaggio, ViaggioDTO.class);


        if (viaggio.getDataInizio() != null) {
            dto.setDataInizio(viaggio.getDataInizio());
        }
        if (viaggio.getDataFine() != null) {
            dto.setDataFine(viaggio.getDataFine());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViaggioDTO> getViaggiByOrganizzatore(Long organizzatoreId) {
        List<Viaggio> viaggi = viaggioRepository.findByOrganizzatoreId(organizzatoreId);
        log.info("Trovati {} viaggi per l'organizzatore id: {}", viaggi.size(), organizzatoreId);

        return viaggi.stream()
                .filter(viaggio -> viaggio.getStato() != null && viaggio.getStato() != Viaggio.StatoViaggio.ANNULLATO)
                .map(viaggio -> {
                    ViaggioDTO dto = modelMapper.map(viaggio, ViaggioDTO.class);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viaggio> getViaggiInPartenza(LocalDate oggi, LocalDate limite) {
        log.info("Recupero viaggi in partenza tra {} e {}", oggi, limite);
        return viaggioRepository.findViaggiInPartenza(oggi, limite);
    }

}