package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Segnalazione;
import com.example.progettoenterprise.data.repositories.*;
import com.example.progettoenterprise.data.repositories.specifications.SegnalazioneSpecification;
import com.example.progettoenterprise.data.service.SegnalazioneService;
import com.example.progettoenterprise.dto.SegnalazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SegnalazioneServiceImpl implements SegnalazioneService {

    private static final int DIMENSIONE_PAGINA = 10;
    private static final String REALM_NAME = "enterprise-realm";

    private final SegnalazioneRepository segnalazioneRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    private final UtenteRepository utenteRepository;
    private final MessaggioChatRepository messaggioChatRepository;
    private final RecensioneRepository recensioneRepository;
    private final ViaggioRepository viaggioRepository;

    private final Keycloak keycloakAdminClient;

    @Override
    @Transactional
    public SegnalazioneDTO creaSegnalazione(SegnalazioneDTO segnalazioneDTO, Long idSegnalatore) {

        String tipoInArrivo = segnalazioneDTO.getTipo();
        if (!"UTENTE".equals(tipoInArrivo) && !"MESSAGGIO".equals(tipoInArrivo) && !"RECENSIONE".equals(tipoInArrivo)) {
            throw new IllegalArgumentException("Tipo di segnalazione non valido. È possibile segnalare solo Utenti, Messaggi o Recensioni.");
        }

        Segnalazione segnalazione = modelMapper.map(segnalazioneDTO, Segnalazione.class);
        segnalazione.setStato(Segnalazione.StatoSegnalazione.APERTA);
        segnalazione.setSegnalatoreId(idSegnalatore);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        log.info("Nuova segnalazione creata con ID: {}", salvata.getId());

        return convertiConNomi(salvata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SegnalazioneDTO> cercaSegnalazioni(SegnalazioneSpecification.SegnalazioneFilter filtro, int pagina) {
        PageRequest richiestaPagina = PageRequest.of(pagina, DIMENSIONE_PAGINA, Sort.by("dataSegnalazione").ascending());
        Page<Segnalazione> paginaSegnalazioni = segnalazioneRepository.findAll(SegnalazioneSpecification.withFilter(filtro), richiestaPagina);

        if ((pagina < 0 || pagina >= paginaSegnalazioni.getTotalPages()) && paginaSegnalazioni.getTotalPages() > 0) {
            log.warn("Tentativo di accesso a una pagina non valida: {}", pagina);
            throw new IllegalArgumentException(messageLang.getMessage("segnalazione.invalid_page"));
        }

        return paginaSegnalazioni.getContent().stream()
                .map(this::convertiConNomi)
                .toList();
    }

    @Override
    @Transactional
    public SegnalazioneDTO prendiInCarico(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        segnalazione.setStato(Segnalazione.StatoSegnalazione.IN_LAVORAZIONE);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        return convertiConNomi(salvata);
    }

    @Transactional
    @Override
    public SegnalazioneDTO risolviSegnalazione(Long idSegnalazione, Long idAdmin, boolean sospendiAutore) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        segnalazione.setStato(Segnalazione.StatoSegnalazione.CHIUSA);
        segnalazione.setAdminId(idAdmin);

        if (segnalazione.getTipo() != null && segnalazione.getIdRiferimento() != null) {

            if (segnalazione.getTipo() == Segnalazione.TipoEntita.UTENTE) {
                if (sospendiAutore) {
                    utenteRepository.findById(segnalazione.getIdRiferimento()).ifPresent(utente -> {
                        utente.setAttivo(false);
                        utente.setMotivoSospensione("Account sospeso a seguito di segnalazione diretta: " + segnalazione.getMotivo());
                        utenteRepository.save(utente);
                        disabilitaSuKeycloak(utente.getUsername());
                        log.info("Utente ID {} sospeso con successo.", utente.getId());
                    });
                }
            }
            else if (segnalazione.getTipo() == Segnalazione.TipoEntita.MESSAGGIO) {
                messaggioChatRepository.findById(segnalazione.getIdRiferimento()).ifPresent(msg -> {
                    String autoreUsername = msg.getMittenteUsername();
                    messaggioChatRepository.delete(msg);
                    log.info("Messaggio rimosso a seguito della segnalazione.");

                    if (sospendiAutore) {
                        utenteRepository.findByUsername(autoreUsername).ifPresent(autore -> {
                            autore.setAttivo(false);
                            autore.setMotivoSospensione("Sospeso per invio di messaggi inappropriati/spam in chat.");
                            utenteRepository.save(autore);
                            disabilitaSuKeycloak(autore.getUsername());
                        });
                    }
                });
            }
            else if (segnalazione.getTipo() == Segnalazione.TipoEntita.RECENSIONE) {
                recensioneRepository.findById(segnalazione.getIdRiferimento()).ifPresent(recensione -> {
                    Long viaggioId = recensione.getViaggio().getId();
                    int votoSottratto = recensione.getVoto();
                    String autoreUsername = recensione.getUtente().getUsername();

                    recensioneRepository.delete(recensione);
                    viaggioRepository.ricalcolaMediaPerEliminazione(viaggioId, votoSottratto);
                    log.info("Recensione rimossa e media ricalcolata per il viaggio ID: {}", viaggioId);

                    if (sospendiAutore) {
                        utenteRepository.findByUsername(autoreUsername).ifPresent(autore -> {
                            autore.setAttivo(false);
                            autore.setMotivoSospensione("Sospeso per pubblicazione di recensioni inappropriate/spam.");
                            utenteRepository.save(autore);
                            disabilitaSuKeycloak(autore.getUsername());
                        });
                    }
                });
            }
        }

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        return convertiConNomi(salvata);
    }

    @Override
    @Transactional
    public SegnalazioneDTO rifiutaSegnalazione(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        segnalazione.setStato(Segnalazione.StatoSegnalazione.RIFIUTATA);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        return convertiConNomi(salvata);
    }

    @Override
    @Transactional(readOnly = true)
    public long contaSegnalazioniAperte() {
        return segnalazioneRepository.countByStato(Segnalazione.StatoSegnalazione.APERTA);
    }

    private void disabilitaSuKeycloak(String username) {
        try {
            List<UserRepresentation> users = keycloakAdminClient.realm(REALM_NAME).users().search(username);
            if (!users.isEmpty()) {
                UserRepresentation kcUser = users.get(0);
                kcUser.setEnabled(false);
                keycloakAdminClient.realm(REALM_NAME).users().get(kcUser.getId()).update(kcUser);
                keycloakAdminClient.realm(REALM_NAME).users().get(kcUser.getId()).logout();
                log.info("Utente {} disabilitato e disconnesso forzatamente da Keycloak.", username);
            }
        } catch (Exception e) {
            log.error("Errore durante la disattivazione su Keycloak dell'utente {}: {}", username, e.getMessage());
        }
    }

    private SegnalazioneDTO convertiConNomi(Segnalazione segnalazione) {
        SegnalazioneDTO dto = modelMapper.map(segnalazione, SegnalazioneDTO.class);

        if (segnalazione.getSegnalatoreId() != null) {
            utenteRepository.findById(segnalazione.getSegnalatoreId())
                    .ifPresent(utente -> dto.setSegnalatoreUsername(utente.getUsername()));
        }

        if (segnalazione.getAdminId() != null) {
            utenteRepository.findById(segnalazione.getAdminId())
                    .ifPresent(admin -> dto.setAdminUsername(admin.getUsername()));
        }

        if (segnalazione.getTipo() != null && segnalazione.getIdRiferimento() != null) {
            switch (segnalazione.getTipo()) {
                case UTENTE:
                    utenteRepository.findById(segnalazione.getIdRiferimento())
                            .ifPresent(u -> dto.setRiferimentoNome(u.getUsername()));
                    break;
                case RECENSIONE:
                    recensioneRepository.findById(segnalazione.getIdRiferimento())
                            .ifPresentOrElse(
                                    rec -> {
                                        String commento = (rec.getCommento() != null && !rec.getCommento().trim().isEmpty()) ? rec.getCommento() : "Nessun commento di testo.";
                                        String anteprima = "Valutazione: " + rec.getVoto() + " Stelle.\n\nTesto: \"" + commento + "\"";
                                        dto.setRiferimentoNome(anteprima);
                                    },
                                    () -> dto.setRiferimentoNome("Recensione già rimossa o inesistente")
                            );
                    break;
                case MESSAGGIO:
                    messaggioChatRepository.findById(segnalazione.getIdRiferimento())
                            .ifPresentOrElse(
                                    msg -> {
                                        String anteprima = "Messaggio di @" + msg.getMittenteUsername() + ": \"" + msg.getTesto() + "\"";
                                        dto.setRiferimentoNome(anteprima);
                                    },
                                    () -> dto.setRiferimentoNome("Messaggio già rimosso o inesistente")
                            );
                    break;
            }
        }
        return dto;
    }
}