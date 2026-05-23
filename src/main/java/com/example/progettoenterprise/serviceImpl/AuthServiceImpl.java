package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.service.AuthService;
import com.example.progettoenterprise.dto.RegistrazioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private static final String REALM_NAME = "enterprise-realm";
    private final MessageLang messageLang;

    // Metodo per registrare un nuovo utente
    @Override
    public UtenteDTO registraUtente(RegistrazioneDTO dto){

        // Controllo sullo username
        List<UserRepresentation> existingUsersByUsername = keycloak.realm(REALM_NAME).users().search(dto.getUsername(), true);
        if (!existingUsersByUsername.isEmpty()) {
            log.warn("Registrazione bloccata: lo username '{}' è già in uso", dto.getUsername());
            throw new IllegalArgumentException(messageLang.getMessage("utente.username.exist", dto.getUsername()));
        }

        // Controllo sull'email
        List<UserRepresentation> existingUsersByEmail = keycloak.realm(REALM_NAME).users().searchByEmail(dto.getEmail(), true);
        if (!existingUsersByEmail.isEmpty()) {
            log.warn("Registrazione bloccata: l'email '{}' è già registrata", dto.getEmail());
            throw new IllegalArgumentException(messageLang.getMessage("utente.email.exist", dto.getEmail()));
        }

        // Creazione dell'utente
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(dto.getUsername());
        keycloakUser.setEmail(dto.getEmail());
        keycloakUser.setFirstName(dto.getNome());
        keycloakUser.setLastName(dto.getCognome());
        keycloakUser.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false); // La password è subito definitiva
        keycloakUser.setCredentials(Collections.singletonList(credential));

        try (Response response = keycloak.realm(REALM_NAME).users().create(keycloakUser)) {
            int status = response.getStatus();

            if (status == 201) {
                String path = response.getLocation().getPath();
                String userId = path.substring(path.lastIndexOf('/') + 1);

                log.info("Utente creato con successo su Keycloak. Id generato: {}. Tentato invio email di verifica...", userId);

                try {
                    keycloak.realm(REALM_NAME).users().get(userId).executeActionsEmail(List.of("VERIFY_EMAIL"));
                    log.info("Email di verifica inviata con successo all'indirizzo dell'utente");
                } catch (Exception e) {
                    log.error("Impossibile inviare l'email di verifica per l'utente ID: {}", userId, e);
                }

                UtenteDTO utenteCreato = new UtenteDTO();
                utenteCreato.setUsername(dto.getUsername());
                utenteCreato.setEmail(dto.getEmail());
                utenteCreato.setNome(dto.getNome());
                utenteCreato.setCognome(dto.getCognome());
                return utenteCreato;

            } else if (status == 409) {
                log.warn("Conflitto imprevisto su Keycloak per: {}", dto.getUsername());
                throw new IllegalArgumentException(messageLang.getMessage("utente.conflict", dto.getUsername()));
            } else {
                log.error("Keycloak ha risposto con un codice HTTP imprevisto: {}", status);
                throw new RuntimeException(messageLang.getMessage("utente.registration_failed"));
            }

        } catch (IllegalArgumentException e) {
            // Rilancio dell'eccezione di conflitto intatta
            throw e;
        } catch (Exception e) {
            // Cattura i problemi di rete o altre eccezioni impreviste
            log.error("Errore critico durante la comunicazione con Keycloak", e);
            throw new RuntimeException(messageLang.getMessage("utente.registration_failed"));
        }
    }
}