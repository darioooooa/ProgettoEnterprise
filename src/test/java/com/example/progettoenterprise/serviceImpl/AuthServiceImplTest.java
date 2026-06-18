package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.dto.RegistrazioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private MessageLang messageLang;

    @InjectMocks
    private AuthServiceImpl authService;

    // Definiamo i mock per l'infrastruttura a cascata (scatole cinesi) di Keycloak
    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;
    @Mock private Response response;

    private RegistrazioneDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new RegistrazioneDTO();
        validDto.setUsername("mario_rossi");
        validDto.setEmail("mario@email.it");
        validDto.setNome("Mario");
        validDto.setCognome("Rossi");
        validDto.setPassword("password123");
    }


    @Test
    @DisplayName("Registrazione: Successo completo (Codice 201)")
    void testRegistraUtente_Successo() throws Exception {
        // Simuliamo che lo username e l'email siano liberi
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search("mario_rossi", true)).thenReturn(Collections.emptyList());
        when(usersResource.searchByEmail("mario@email.it", true)).thenReturn(Collections.emptyList());

        // Simuliamo la risposta positiva (201 Created) da Keycloak
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);

        // Simuliamo l'estrazione dell'ID dalla posizione (URI) restituita da Keycloak
        when(response.getLocation()).thenReturn(new URI("http://localhost/auth/admin/realms/enterprise-realm/users/abc-123-id"));

        // Simuliamo l'invio dell'email di verifica
        when(usersResource.get("abc-123-id")).thenReturn(userResource);
        doNothing().when(userResource).executeActionsEmail(List.of("VERIFY_EMAIL"));

        UtenteDTO risultato = authService.registraUtente(validDto);

        assertNotNull(risultato);
        assertEquals("mario_rossi", risultato.getUsername());
        assertEquals("mario@email.it", risultato.getEmail());

        // Verifichiamo che i metodi corretti siano stati chiamati
        verify(usersResource, times(1)).create(any(UserRepresentation.class));
        verify(userResource, times(1)).executeActionsEmail(List.of("VERIFY_EMAIL"));
    }

    @Test
    @DisplayName("Registrazione: Successo 201 ma fallisce l'invio dell'email")
    void testRegistraUtente_Successo_MaEmailFallisce() throws Exception {
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(anyString(), anyBoolean())).thenReturn(Collections.emptyList());
        when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(new URI("http://localhost/.../users/abc-123-id"));

        when(usersResource.get("abc-123-id")).thenReturn(userResource);

        // Facciamo fallire l'invio dell'email: il sistema DEVE catturare l'errore e andare avanti
        doThrow(new RuntimeException("Server SMTP offline")).when(userResource).executeActionsEmail(anyList());

        UtenteDTO risultato = authService.registraUtente(validDto);

        assertNotNull(risultato);
        assertEquals("mario_rossi", risultato.getUsername());
    }

    @Test
    @DisplayName("Registrazione: Errore - Username già in uso")
    void testRegistraUtente_ErroreUsernameEsistente() {
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search("mario_rossi", true)).thenReturn(List.of(new UserRepresentation()));
        when(messageLang.getMessage("utente.username.exist", "mario_rossi")).thenReturn("Username occupato");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            authService.registraUtente(validDto);
        });

        assertEquals("Username occupato", eccezione.getMessage());
        verify(usersResource, never()).create(any());
    }

    @Test
    @DisplayName("Registrazione: Errore - Email già in uso")
    void testRegistraUtente_ErroreEmailEsistente() {
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search("mario_rossi", true)).thenReturn(Collections.emptyList());

        when(usersResource.searchByEmail("mario@email.it", true)).thenReturn(List.of(new UserRepresentation()));

        when(messageLang.getMessage("utente.email.exist", "mario@email.it")).thenReturn("Email occupata");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            authService.registraUtente(validDto);
        });

        assertEquals("Email occupata", eccezione.getMessage());
        verify(usersResource, never()).create(any());
    }

    @Test
    @DisplayName("Registrazione: Errore 409 Conflitto su Keycloak")
    void testRegistraUtente_Errore409Conflict() {
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(anyString(), anyBoolean())).thenReturn(Collections.emptyList());
        when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        when(messageLang.getMessage("utente.conflict", "mario_rossi")).thenReturn("Conflitto di rete imprevisto");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            authService.registraUtente(validDto);
        });

        assertEquals("Conflitto di rete imprevisto", eccezione.getMessage());
    }

    @Test
    @DisplayName("Registrazione: Errore HTTP Sconosciuto")
    void testRegistraUtente_ErroreHTTPGenerico() {
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(anyString(), anyBoolean())).thenReturn(Collections.emptyList());
        when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(500);

        when(messageLang.getMessage("utente.registration_failed")).thenReturn("Registrazione fallita");

        RuntimeException eccezione = assertThrows(RuntimeException.class, () -> {
            authService.registraUtente(validDto);
        });

        assertEquals("Registrazione fallita", eccezione.getMessage());
    }

    @Test
    @DisplayName("Registrazione: Eccezione generica (es. Keycloak offline o Crash)")
    void testRegistraUtente_CrashGenerico() {
        when(keycloak.realm("enterprise-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(anyString(), anyBoolean())).thenReturn(Collections.emptyList());
        when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        when(usersResource.create(any(UserRepresentation.class))).thenThrow(new RuntimeException("Connection Refused"));

        when(messageLang.getMessage("utente.registration_failed")).thenReturn("Errore critico di sistema");

        RuntimeException eccezione = assertThrows(RuntimeException.class, () -> {
            authService.registraUtente(validDto);
        });

        assertEquals("Errore critico di sistema", eccezione.getMessage());
    }
}