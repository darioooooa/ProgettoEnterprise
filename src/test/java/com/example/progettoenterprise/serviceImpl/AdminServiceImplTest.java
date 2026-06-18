package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.Organizzatore;
import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private RichiestaPromozioneRepository richiestaRepository;

    @Mock
    private OrganizzatoreRepository organizzatoreRepository;

    @Mock
    private Keycloak keycloak;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("Rifiuto richiesta: Caso di successo")
    void testRifiutaRichiesta_Successo() {
        Long idRichiesta = 1L;
        Long idAdmin = 99L;
        String note = "Mancano documenti validi";

        RichiestaPromozione richiestaMock = new RichiestaPromozione();
        richiestaMock.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA);

        when(richiestaRepository.findById(idRichiesta)).thenReturn(Optional.of(richiestaMock));

        adminService.rifiutaRichiesta(idRichiesta, note, idAdmin);

        assertEquals(RichiestaPromozione.StatoRichiesta.RIFIUTATA, richiestaMock.getStato());
        assertEquals(note, richiestaMock.getMotivazione());
        assertEquals(idAdmin, richiestaMock.getAdminId());
        verify(richiestaRepository, times(1)).save(richiestaMock);
    }

    @Test
    @DisplayName("Rifiuto richiesta: Errore (Richiesta non In Attesa)")
    void testRifiutaRichiesta_NonInAttesa() {
        Long idRichiesta = 1L;
        RichiestaPromozione richiestaMock = new RichiestaPromozione();
        richiestaMock.setStato(RichiestaPromozione.StatoRichiesta.APPROVATA); // Già processata!

        when(richiestaRepository.findById(idRichiesta)).thenReturn(Optional.of(richiestaMock));

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            adminService.rifiutaRichiesta(idRichiesta, "note", 99L);
        });

        assertTrue(eccezione.getMessage().contains("già stata approvata"));
        verify(richiestaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Approvazione richiesta: Caso di successo con integrazione Keycloak")
    void testApprovaRichiesta_Successo() {
        Long idRichiesta = 1L;
        Long idAdmin = 99L;
        String username = "nuovoOrg";
        String email = "org@email.it";

        // Preparazione Dati Locali
        Viaggiatore viaggiatore = new Viaggiatore();
        viaggiatore.setNome("Mario");
        viaggiatore.setCognome("Rossi");

        RichiestaPromozione richiestaMock = new RichiestaPromozione();
        richiestaMock.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA);
        richiestaMock.setViaggiatore(viaggiatore);
        richiestaMock.setUsernameRichiesto(username);
        richiestaMock.setEmailProfessionale(email);

        when(richiestaRepository.findById(idRichiesta)).thenReturn(Optional.of(richiestaMock));
        RealmResource realmResourceMock = mock(RealmResource.class);
        UsersResource usersResourceMock = mock(UsersResource.class);
        RolesResource rolesResourceMock = mock(RolesResource.class);
        RoleResource roleResourceMock = mock(RoleResource.class);
        UserResource userResourceMock = mock(UserResource.class);
        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);
        Response responseMock = mock(Response.class);

        when(keycloak.realm("enterprise-realm")).thenReturn(realmResourceMock);

        // Creazione Utente
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
        when(responseMock.getStatus()).thenReturn(201); // 201 = Created

        // Ricerca Utente appena creato
        UserRepresentation kcUserMock = new UserRepresentation();
        kcUserMock.setId("kc-secret-id-123");
        when(usersResourceMock.search(username)).thenReturn(List.of(kcUserMock));

        //  Recupero Ruolo Organizzatore
        RoleRepresentation orgRole = new RoleRepresentation();
        orgRole.setName("ORGANIZZATORE");
        when(realmResourceMock.roles()).thenReturn(rolesResourceMock);
        when(rolesResourceMock.get("ORGANIZZATORE")).thenReturn(roleResourceMock);
        when(roleResourceMock.toRepresentation()).thenReturn(orgRole);

        when(usersResourceMock.get("kc-secret-id-123")).thenReturn(userResourceMock);
        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        adminService.approvaRichiesta(idRichiesta, idAdmin);

        // Verifiche Locali
        assertEquals(RichiestaPromozione.StatoRichiesta.APPROVATA, richiestaMock.getStato());
        assertEquals(idAdmin, richiestaMock.getAdminId());
        verify(organizzatoreRepository, times(1)).save(any(Organizzatore.class));
        verify(richiestaRepository, times(1)).save(richiestaMock);

        // Verifiche Esterne
        verify(roleScopeResourceMock, times(1)).add(anyList()); // Ruolo assegnato
        verify(userResourceMock, times(1)).executeActionsEmail(List.of("UPDATE_PASSWORD")); // Email inviata
    }

    @Test
    @DisplayName("Approvazione richiesta: Errore (Utente esiste già su Keycloak)")
    void testApprovaRichiesta_ConflittoKeycloak() {
        Long idRichiesta = 1L;
        RichiestaPromozione richiestaMock = new RichiestaPromozione();
        richiestaMock.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA);
        richiestaMock.setViaggiatore(new Viaggiatore());

        when(richiestaRepository.findById(idRichiesta)).thenReturn(Optional.of(richiestaMock));

        RealmResource realmResourceMock = mock(RealmResource.class);
        UsersResource usersResourceMock = mock(UsersResource.class);
        Response responseMock = mock(Response.class);

        when(keycloak.realm("enterprise-realm")).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
        when(responseMock.getStatus()).thenReturn(409); // 409 = Conflict

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            adminService.approvaRichiesta(idRichiesta, 99L);
        });

        assertTrue(eccezione.getMessage().contains("esiste già nel sistema di sicurezza"));
    }

    @Test
    @DisplayName("Lettura richieste: Caso di successo")
    void testGetRichieste() {
        RichiestaPromozione r1 = new RichiestaPromozione();
        r1.setId(1L);
        r1.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA);

        Viaggiatore v = new Viaggiatore();
        v.setUsername("viaggiatoreTest");
        r1.setViaggiatore(v);

        when(richiestaRepository.findAll()).thenReturn(List.of(r1));

        List<RichiestaPromozioneDTO> risultati = adminService.getRichieste();

        assertEquals(1, risultati.size());
        assertEquals("IN_ATTESA", risultati.get(0).getStato());
        assertEquals("viaggiatoreTest", risultati.get(0).getUsernameViaggiatore());
    }

    @Test
    @DisplayName("Ban utente: Caso di successo")
    void testBanUtente_Successo() {
        Long idUtente = 5L;
        Utente viaggiatoreMock = new Viaggiatore(); // Non è un organizzatore
        viaggiatoreMock.setUsername("badUser");
        viaggiatoreMock.setEmail("bad@email.it");
        viaggiatoreMock.setAttivo(true);

        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(viaggiatoreMock));

        adminService.banUtente(idUtente);

        assertFalse(viaggiatoreMock.isAttivo()); // Utente disattivato
        assertEquals("Violazione dei termini di servizio", viaggiatoreMock.getMotivoSospensione());
        verify(utenteRepository, times(1)).save(viaggiatoreMock);
        verify(emailService, times(1)).inviaEmailBan("bad@email.it", "badUser");
    }

    @Test
    @DisplayName("Ban utente: Errore (Tentativo di bannare un Organizzatore)")
    void testBanUtente_ErroreOrganizzatore() {
        Long idUtente = 5L;
        Utente organizzatoreMock = new Organizzatore();
        organizzatoreMock.setRuolo(Utente.Ruolo.ROLE_ORGANIZZATORE); // È protetto

        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(organizzatoreMock));

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            adminService.banUtente(idUtente);
        });

        assertTrue(eccezione.getMessage().contains("non puoi bannare un account Organizzatore"));
        verify(utenteRepository, never()).save(any());
        verify(emailService, never()).inviaEmailBan(anyString(), anyString());
    }

    @Test
    @DisplayName("Lettura utenti bannati: Caso di successo")
    void testGetUtentiBannati() {
        Utente u1 = mock(Utente.class);
        UtenteDTO dtoMock = new UtenteDTO();

        when(utenteRepository.findByIsAttivoFalse()).thenReturn(List.of(u1));
        when(modelMapper.map(u1, UtenteDTO.class)).thenReturn(dtoMock);

        List<UtenteDTO> risultati = adminService.getUtentiBannati();

        assertEquals(1, risultati.size());
    }

    @Test
    @DisplayName("Sban utente: Caso di successo")
    void testSbannaUtente_Successo() {
        Long idUtente = 5L;
        String username = "utenteRedento";

        Utente utenteMock = new Viaggiatore();
        utenteMock.setId(idUtente);
        utenteMock.setUsername(username);
        utenteMock.setAttivo(false);
        utenteMock.setMotivoSospensione("Perdonato");

        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(utenteMock));

        // Mock Keycloak per la riabilitazione
        RealmResource realmResourceMock = mock(RealmResource.class);
        UsersResource usersResourceMock = mock(UsersResource.class);
        UserResource userResourceMock = mock(UserResource.class);

        UserRepresentation kcUserMock = new UserRepresentation();
        kcUserMock.setId("kc-id-999");
        kcUserMock.setEnabled(false);

        when(keycloak.realm("enterprise-realm")).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.search(username)).thenReturn(List.of(kcUserMock));
        when(usersResourceMock.get("kc-id-999")).thenReturn(userResourceMock);

        adminService.sbannaUtente(idUtente);

        // Verifiche Locali
        assertTrue(utenteMock.isAttivo());
        assertNull(utenteMock.getMotivoSospensione());
        verify(utenteRepository, times(1)).save(utenteMock);
        // Verifiche Esterne
        assertTrue(kcUserMock.isEnabled()); // Verifichiamo che il DTO Keycloak sia stato acceso
        verify(userResourceMock, times(1)).update(kcUserMock); // Comando inviato a Keycloak
    }
}