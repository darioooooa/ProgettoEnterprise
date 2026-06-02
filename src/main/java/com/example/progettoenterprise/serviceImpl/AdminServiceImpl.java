package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    private static final String REALM_NAME = "enterprise-realm";

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

        // Creazione dell'organizzatore
        String nuovoUsername = richiesta.getUsernameRichiesto();
        String nuovaEmail = richiesta.getEmailProfessionale();
        Organizzatore nuovoOrg = new Organizzatore();
        nuovoOrg.setUsername(nuovoUsername);
        nuovoOrg.setEmail(nuovaEmail);
        nuovoOrg.setNome(viaggiatore.getNome());
        nuovoOrg.setCognome(viaggiatore.getCognome());
        nuovoOrg.setRuolo(Utente.Ruolo.ROLE_ORGANIZZATORE);
        nuovoOrg.setAttivo(true);
        organizzatoreRepository.save(nuovoOrg);

        // Aggiornamento della richiesta
        richiesta.setStato(RichiestaPromozione.StatoRichiesta.APPROVATA);
        richiesta.setDataValutazione(LocalDateTime.now());
        richiesta.setAdminId(adminIdCorrente);
        richiestaRepository.save(richiesta);

        // Sincronizzazione keycloak
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(nuovoUsername);
        keycloakUser.setEmail(nuovaEmail);
        keycloakUser.setFirstName(viaggiatore.getNome());
        keycloakUser.setLastName(viaggiatore.getCognome());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(true);

        // Impostazione password temporanea
        String passwordTemporanea = UUID.randomUUID().toString().substring(0, 8);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(passwordTemporanea);
        // Credenziali temporanee. L'utente dovrà cambiare la password
        credential.setTemporary(true);
        keycloakUser.setCredentials(Collections.singletonList(credential));

        // Crea il nuovo utente su keycloak
        Response response = keycloak.realm(REALM_NAME).users().create(keycloakUser);

        if (response.getStatus() == 201) {
            List<UserRepresentation> searchResult = keycloak.realm(REALM_NAME).users().search(nuovoUsername);
            if (!searchResult.isEmpty()) {
                String keycloakUserId = searchResult.getFirst().getId();

                // Recupera e assegna il ruolo ORGANIZZATORE da keycloak
                RoleRepresentation orgRole = keycloak.realm(REALM_NAME).roles().get("ORGANIZZATORE").toRepresentation();
                keycloak.realm(REALM_NAME).users().get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(orgRole));

                try {
                    // Forza l'invio dell'email di aggiornamento password
                    keycloak.realm(REALM_NAME).users().get(keycloakUserId).executeActionsEmail(List.of("UPDATE_PASSWORD"));
                    log.info("Email di aggiornamento password inviata con successo all'organizzatore con email: {}", nuovaEmail);
                }catch (Exception e){
                    log.error("Impossibile inviare l'email di aggiornamento password per l'utente id: {}", keycloakUserId, e);
                }

                // L'utente dovrà cambiare password, tramite il link inviato sull'email indicata
            }
        } else if (response.getStatus() == 409) {
            // Email o username già esistenti su keycloak
            log.warn("Sincronizzazione fallita: l'utente {} esiste già su Keycloak", nuovoUsername);
            throw new IllegalArgumentException("Impossibile promuovere: L'utente " + nuovoUsername + " esiste già nel sistema di sicurezza.");
        } else {
            log.error("Errore durante la creazione su Keycloak: {}", response.readEntity(String.class));
            throw new RuntimeException("Errore durante la comunicazione con Keycloak. Status: " + response.getStatus());
        }
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
                    dto.setDocumentiLink(richiesta.getDocumentiLink());
                    dto.setAdminId(richiesta.getAdminId());
                    dto.setUsernameRichiesto(richiesta.getUsernameRichiesto());
                    dto.setEmailProfessionale(richiesta.getEmailProfessionale());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void rifiutaRichiesta(Long richiestaId, String noteAdmin, Long adminIdCorrente) {
        RichiestaPromozione richiesta = richiestaRepository.findById(richiestaId)
                .orElseThrow(() -> {
                    log.warn("Impossibile rifiutare la richiesta: richiesta ID {} non trovata", richiestaId);
                    return new EntityNotFoundException("Richiesta non trovata");
                });

        if (richiesta.getStato() != RichiestaPromozione.StatoRichiesta.IN_ATTESA) {
            log.warn("Impossibile rifiutare la richiesta: richiesta ID {} non in stato IN_ATTESA", richiestaId);
            throw new IllegalArgumentException("Impossibile procedere: la richiesta è già stata " + richiesta.getStato().name().toLowerCase() + ".");
        }

        richiesta.setStato(RichiestaPromozione.StatoRichiesta.RIFIUTATA);
        richiesta.setMotivazione(noteAdmin);
        richiesta.setDataValutazione(LocalDateTime.now());
        richiesta.setAdminId(adminIdCorrente);
        richiestaRepository.save(richiesta);
    }

    @Override
    @Transactional
    public void banUtente(Long userId) {
        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        utente.setAttivo(false);
        utente.setMotivoSospensione("Violazione dei termini di servizio");
        utenteRepository.save(utente);

        emailService.inviaEmailBan(utente.getEmail(), utente.getUsername());
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

        log.info("L'utente ID {} ({}) è stato riattivato con successo.", utente.getId(), utente.getUsername());
    }
}

