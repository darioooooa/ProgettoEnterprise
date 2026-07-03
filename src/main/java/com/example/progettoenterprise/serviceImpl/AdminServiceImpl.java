package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.specifications.RichiestaPromozioneSpecification;
import com.example.progettoenterprise.data.service.AdminService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.core.io.Resource;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UtenteRepository utenteRepository;
    private final RichiestaPromozioneRepository richiestaRepository;
    private final OrganizzatoreRepository organizzatoreRepository;
    private final Keycloak keycloak;
    private final EmailServiceImpl emailService;
    private final ModelMapper modelMapper;

    private final String REALM_NAME = "enterprise-realm";
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    @Transactional
    public void approvaRichiesta(Long richiestaId, Long adminIdCorrente) {
        RichiestaPromozione richiesta = richiestaRepository.findById(richiestaId)
                .orElseThrow(() -> {
                    log.warn("Impossibile approvare la richiesta: richiesta ID {} non trovata", richiestaId);
                    return new EntityNotFoundException("Richiesta non trovata");
                });

        if (richiesta.getStato() != RichiestaPromozione.StatoRichiesta.IN_ATTESA) {
            log.warn("Impossibile approvare la richiesta: richiesta ID {} non in stato IN_ATTESA", richiestaId);
            throw new IllegalArgumentException("Impossibile procedere: la richiesta è già stata " + richiesta.getStato().name().toLowerCase() + ".");
        }

        Viaggiatore viaggiatore = richiesta.getViaggiatore();
        String nuovoUsername = richiesta.getUsernameRichiesto();
        String nuovaEmail = richiesta.getEmailProfessionale();


        List<UserRepresentation> existingUsers = keycloak.realm(REALM_NAME).users().search(nuovoUsername, true);
        if (!existingUsers.isEmpty()) {
            log.warn("Impossibile promuovere: l'utente {} esiste già su Keycloak", nuovoUsername);
            throw new IllegalArgumentException("Impossibile promuovere: l'username '" + nuovoUsername + "' è già in uso su Keycloak.");
        }

        Organizzatore nuovoOrg = new Organizzatore();
        nuovoOrg.setUsername(nuovoUsername);
        nuovoOrg.setEmail(nuovaEmail);
        nuovoOrg.setNome(viaggiatore.getNome());
        nuovoOrg.setCognome(viaggiatore.getCognome());
        nuovoOrg.setRuolo(Utente.Ruolo.ROLE_ORGANIZZATORE);
        nuovoOrg.setAttivo(true);
        organizzatoreRepository.save(nuovoOrg);

        richiesta.setStato(RichiestaPromozione.StatoRichiesta.APPROVATA);
        richiesta.setDataValutazione(LocalDateTime.now());
        richiesta.setAdminId(adminIdCorrente);
        richiestaRepository.save(richiesta);

        CompletableFuture.runAsync(() -> {
            try {

                UserRepresentation keycloakUser = new UserRepresentation();
                keycloakUser.setUsername(nuovoUsername);
                keycloakUser.setEmail(nuovaEmail);
                keycloakUser.setFirstName(viaggiatore.getNome());
                keycloakUser.setLastName(viaggiatore.getCognome());
                keycloakUser.setEnabled(true);
                keycloakUser.setEmailVerified(true);

                String passwordTemporanea = UUID.randomUUID().toString().substring(0, 8);
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(passwordTemporanea);
                credential.setTemporary(true);
                keycloakUser.setCredentials(Collections.singletonList(credential));

                Response response = keycloak.realm(REALM_NAME).users().create(keycloakUser);

                if (response.getStatus() == 201) {
                    List<UserRepresentation> searchResult = keycloak.realm(REALM_NAME).users().search(nuovoUsername);
                    if (!searchResult.isEmpty()) {
                        String keycloakUserId = searchResult.getFirst().getId();

                        // Assegna ruolo
                        RoleRepresentation orgRole = keycloak.realm(REALM_NAME).roles().get("ORGANIZZATORE").toRepresentation();
                        keycloak.realm(REALM_NAME).users().get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(orgRole));

                        // Invia email di cambio password (Keycloak)
                        try {
                            keycloak.realm(REALM_NAME).users().get(keycloakUserId).executeActionsEmail(List.of("UPDATE_PASSWORD"));
                            log.info("✅ Email Keycloak inviata a: {}", nuovaEmail);
                        } catch (Exception e) {
                            log.error("❌ Impossibile inviare email Keycloak per id {}: {}", keycloakUserId, e.getMessage());
                        }

                        // Invia email di benvenuto personalizzata
                        try {
                            String oggetto = "La tua candidatura come Organizzatore è stata approvata!";
                            String testo = "Gentile " + viaggiatore.getNome() + ",\n\n" +
                                    "Siamo lieti di informarti che la tua richiesta per diventare Organizzatore su Enterprise è stata approvata!\n\n" +
                                    "Username: " + nuovoUsername + "\n" +
                                    "Password temporanea: " + passwordTemporanea + "\n\n" +
                                    "Al primo accesso ti verrà richiesto di cambiare la password.\n\n" +
                                    "Benvenuto a bordo!\nIl Team di Enterprise.";

                            emailService.sendSimpleEmail(nuovaEmail, oggetto, testo);
                            log.info("✅ Email di benvenuto inviata a: {}", nuovaEmail);
                        } catch (Exception e) {
                            log.error("❌ Errore nell'invio dell'email di benvenuto a {}: {}", nuovaEmail, e.getMessage());
                        }
                    }
                } else {
                    log.error("❌ Errore Keycloak. Status: {}", response.getStatus());
                }
            } catch (Exception e) {
                log.error("❌ Errore operazioni asincrone Keycloak per {}: {}", nuovoUsername, e.getMessage());
            }
        });

        log.info("✅ Richiesta {} approvata. Operazioni Keycloak in background.", richiestaId);
    }

    @Override
    public List<RichiestaPromozioneDTO> getRichieste() {
        return richiestaRepository.findAll()
                .stream()
                .map(richiesta -> {
                    RichiestaPromozioneDTO dto = new RichiestaPromozioneDTO();
                    dto.setId(richiesta.getId());
                    dto.setUsernameViaggiatore(richiesta.getViaggiatore().getUsername());
                    dto.setEmailViaggiatore(richiesta.getViaggiatore().getEmail());
                    dto.setDataRichiesta(richiesta.getDataRichiesta());
                    dto.setMotivazione(richiesta.getMotivazione());
                    dto.setStato(richiesta.getStato().name());
                    dto.setBiografiaProfessionale(richiesta.getBiografiaProfessionale());
                    dto.setAdminId(richiesta.getAdminId());
                    dto.setUsernameRichiesto(richiesta.getUsernameRichiesto());
                    dto.setEmailProfessionale(richiesta.getEmailProfessionale());
                    dto.setDocumentiLink(richiesta.getDocumentiLink());
                    return dto;
                })
                .toList();
    }

    @Override
    public Page<RichiestaPromozioneDTO> getRichiestePaginate(RichiestaPromozioneSpecification.RichiestaFilter filter, int page, int size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("dataRichiesta").descending());

        org.springframework.data.jpa.domain.Specification<RichiestaPromozione> spec =
                RichiestaPromozioneSpecification.withFilter(filter);

        org.springframework.data.domain.Page<RichiestaPromozione> richiestePage = richiestaRepository.findAll(spec, pageable);

        return richiestePage.map(richiesta -> {
            RichiestaPromozioneDTO dto = new RichiestaPromozioneDTO();
            dto.setId(richiesta.getId());
            dto.setUsernameViaggiatore(richiesta.getViaggiatore().getUsername());
            dto.setEmailViaggiatore(richiesta.getViaggiatore().getEmail());
            dto.setDataRichiesta(richiesta.getDataRichiesta());
            dto.setMotivazione(richiesta.getMotivazione());
            dto.setStato(richiesta.getStato().name());
            dto.setBiografiaProfessionale(richiesta.getBiografiaProfessionale());
            dto.setAdminId(richiesta.getAdminId());
            dto.setUsernameRichiesto(richiesta.getUsernameRichiesto());
            dto.setEmailProfessionale(richiesta.getEmailProfessionale());
            dto.setDocumentiLink(richiesta.getDocumentiLink());
            return dto;
        });
    }

    @Override
    @Transactional
    public void rifiutaRichiesta(Long richiestaId, String noteAdmin, Long adminIdCorrente) {
        RichiestaPromozione richiesta = richiestaRepository.findById(richiestaId)
                .orElseThrow(() -> new EntityNotFoundException("Richiesta non trovata"));

        if (richiesta.getStato() != RichiestaPromozione.StatoRichiesta.IN_ATTESA) {
            throw new IllegalArgumentException("La richiesta è già stata " + richiesta.getStato().name().toLowerCase() + ".");
        }

        richiesta.setStato(RichiestaPromozione.StatoRichiesta.RIFIUTATA);
        richiesta.setMotivazione(noteAdmin);
        richiesta.setDataValutazione(LocalDateTime.now());
        richiesta.setAdminId(adminIdCorrente);
        richiestaRepository.save(richiesta);

        CompletableFuture.runAsync(() -> {
            try {
                String oggetto = "Esito della tua candidatura come Organizzatore";
                String testo = "Gentile utente,\n\n" +
                        "La tua richiesta per diventare Organizzatore su Enterprise è stata valutata dall'amministrazione.\n" +
                        "Purtroppo la candidatura non è stata accettata per il seguente motivo:\n\n" +
                        "--------------------------------------------------\n" +
                        noteAdmin + "\n" +
                        "--------------------------------------------------\n\n" +
                        "Ti invitiamo a consultare il nostro regolamento e, se lo ritieni opportuno, correggere le informazioni " +
                        "per inviare una nuova candidatura in futuro.\n\n" +
                        "Cordiali saluti,\nIl Team di Enterprise.";

                emailService.sendSimpleEmail(richiesta.getEmailProfessionale(), oggetto, testo);
                log.info("✅ Email di rifiuto inviata a: {}", richiesta.getEmailProfessionale());
            } catch (Exception e) {
                log.error("❌ Errore nell'invio dell'email di rifiuto a {}: {}", richiesta.getEmailProfessionale(), e.getMessage());
            }
        });

        log.info("✅ Richiesta {} rifiutata. Email in background.", richiestaId);
    }

    @Override
    @Transactional
    public void banUtente(Long userId) {
        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (utente.getRuolo() == Utente.Ruolo.ROLE_ORGANIZZATORE) {
            throw new IllegalArgumentException("Non puoi bannare un Organizzatore.");
        }

        utente.setAttivo(false);
        utente.setMotivoSospensione("Violazione dei termini");
        utenteRepository.save(utente);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.inviaEmailBan(utente.getEmail(), utente.getUsername());
                log.info("✅ Email di ban inviata a: {}", utente.getEmail());
            } catch (Exception e) {
                log.error("❌ Errore invio email di ban: {}", e.getMessage());
            }
        });
    }

    @Override
    public List<UtenteDTO> getUtentiBannati() {
        return utenteRepository.findByIsAttivoFalse()
                .stream()
                .map(utente -> modelMapper.map(utente, UtenteDTO.class))
                .toList();
    }

    @Transactional
    @Override
    public void sbannaUtente(Long userId) {
        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        utente.setAttivo(true);
        utente.setMotivoSospensione(null);
        utenteRepository.save(utente);

        CompletableFuture.runAsync(() -> {
            try {
                riabilitaSuKeycloak(utente.getUsername());
                log.info("✅ Utente {} riabilitato su Keycloak", utente.getUsername());
            } catch (Exception e) {
                log.error("❌ Errore riabilitazione Keycloak: {}", e.getMessage());
            }
        });
    }

    private void riabilitaSuKeycloak(String username) {
        try {
            List<UserRepresentation> users = keycloak.realm(REALM_NAME).users().search(username);
            if (!users.isEmpty()) {
                UserRepresentation kcUser = users.get(0);
                kcUser.setEnabled(true);
                keycloak.realm(REALM_NAME).users().get(kcUser.getId()).update(kcUser);
            }
        } catch (Exception e) {
            log.error("Errore Keycloak riabilitazione: {}", e.getMessage());
        }
    }

    @Override
    public Resource scaricaDocumentoCandidatura(Long idRichiesta) {
        RichiestaPromozione richiesta = richiestaRepository.findById(idRichiesta)
                .orElseThrow(() -> new EntityNotFoundException("Richiesta non trovata"));

        String objectName = richiesta.getDocumentiLink();
        if (objectName == null || objectName.isBlank()) {
            throw new RuntimeException("Nessun documento associato");
        }

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            byte[] bytes = stream.readAllBytes();
            stream.close();

            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    int underscoreIndex = objectName.indexOf("_");
                    return underscoreIndex != -1 ? objectName.substring(underscoreIndex + 1) : objectName;
                }
            };
        } catch (Exception e) {
            log.error("Errore recupero file da MinIO", e);
            throw new RuntimeException("Errore durante il recupero del documento", e);
        }
    }
}