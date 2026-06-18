package com.example.progettoenterprise.serviceImpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import com.example.progettoenterprise.data.repositories.specifications.UtenteSpecification.UtenteFilter;
import java.util.List;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Usiamo Mockito per gestire le annotazioni @Mock
@ExtendWith(MockitoExtension.class)
public class UtenteServiceImplTest {

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageLang messageLang;

    @Mock
    private Keycloak keycloak;

    @InjectMocks
    private UtenteServiceImpl utenteService;

    @Test
    @DisplayName("Ricerca profilo tramite ID: Caso di successo")
    void testGetProfilo() {
        Long idUtente = 1L;

        // creiamo un mock per il test
        Utente utenteMock = mock(Utente.class);
        UtenteDTO dtoAtteso = new UtenteDTO();
        dtoAtteso.setId(idUtente);

        // Quando cercano utente con id=1 ritornano l'utenteMock
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(utenteMock));

        // Quando chiedono di mappare l'utenteMock restituiamo il dtoAtteso
        when(modelMapper.map(utenteMock, UtenteDTO.class)).thenReturn(dtoAtteso);

        UtenteDTO risultato = utenteService.getProfiloById(idUtente);

        assertNotNull(risultato);
        assertEquals(idUtente, risultato.getId());
        verify(utenteRepository, times(1)).findById(idUtente);
    }

    @Test
    @DisplayName("Ricerca profilo tramite ID: Caso di errore (Utente non trovato)")
    void testProfiloNonTrovato() {
        Long idUtente = 99L;
        String messaggioErroreAtteso = "L'utente con id " + idUtente + " non esiste";

        // Se ti chiedono l'ID 99 rispondiamo che è vuoto
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.empty());

        // Istruiamo il traduttore dei messaggi
        when(messageLang.getMessage("utente.notexist", idUtente)).thenReturn(messaggioErroreAtteso);

        // Controlliamo che l'esecuzione del metodo scateni esattamente quell'errore
        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            utenteService.getProfiloById(idUtente);
        });

        // Verifichiamo che il testo dell'errore sia corretto
        assertEquals(messaggioErroreAtteso, eccezione.getMessage());

        // Siccome il sistema deve essersi bloccato prima, ci assicuriamo che
        // lo strumento di conversione (ModelMapper) non sia mai stato interpellato
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("Ricerca profilo tramite Username: Caso di successo")
    void testFindByUsernameSuccesso() {
        String username = "mariorossi";
        Utente utenteMock = mock(Utente.class);
        UtenteDTO dtoAtteso = new UtenteDTO();
        dtoAtteso.setUsername(username);

        when(utenteRepository.findByUsername(username)).thenReturn(Optional.of(utenteMock));
        when(modelMapper.map(utenteMock, UtenteDTO.class)).thenReturn(dtoAtteso);

        UtenteDTO risultato = utenteService.findByUsername(username);

        assertNotNull(risultato);
        assertEquals(username, risultato.getUsername());
    }

    @Test
    @DisplayName("Ricerca profilo tramite Username: Errore (Utente non trovato)")
    void testFindByUsernameNonTrovato() {
        String username = "inesistente";
        String messaggioErroreAtteso = "Lo username " + username + " non esiste";

        when(utenteRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.username_notexist", username)).thenReturn(messaggioErroreAtteso);

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            utenteService.findByUsername(username);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
    }

    @Test
    @DisplayName("Aggiornamento profilo: Caso di successo")
    void testAggiornaProfiloSuccesso() {
        Long idUtente = 1L;

        UtenteDTO datiInviati = new UtenteDTO();
        datiInviati.setNome("Luca");
        datiInviati.setCognome("Bianchi");

        Utente utenteDalDatabase = mock(Utente.class);
        Utente utenteSalvato = mock(Utente.class);
        UtenteDTO dtoRisposta = new UtenteDTO();
        dtoRisposta.setNome("Luca");

        // Istruiamo il finto database e il convertitore
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(utenteDalDatabase));
        when(utenteRepository.save(utenteDalDatabase)).thenReturn(utenteSalvato);
        when(modelMapper.map(utenteSalvato, UtenteDTO.class)).thenReturn(dtoRisposta);

        UtenteDTO risultato = utenteService.aggiornaProfilo(idUtente, datiInviati);

        assertNotNull(risultato);

        // Controlliamo che il sistema abbia effettivamente provato a cambiare nome e cognome
        verify(utenteDalDatabase).setNome("Luca");
        verify(utenteDalDatabase).setCognome("Bianchi");

        // Controlliamo che il salvataggio sia stato lanciato una volta sola
        verify(utenteRepository, times(1)).save(utenteDalDatabase);
    }

    @Test
    @DisplayName("Aggiornamento profilo: Errore (Utente non trovato)")
    void testAggiornaProfiloNonTrovato() {
        Long idUtente = 99L;
        UtenteDTO datiInviati = new UtenteDTO();
        String messaggioErroreAtteso = "L'utente con id " + idUtente + " non esiste";

        when(utenteRepository.findById(idUtente)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", idUtente)).thenReturn(messaggioErroreAtteso);

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            utenteService.aggiornaProfilo(idUtente, datiInviati);
        });
        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        // Ci assicuriamo che il sistema non abbia mai tentato di salvare nulla
        verify(utenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminazione account: Caso di successo")
    void testEliminaAccountSuccesso() {
        Long idUtente = 1L;

        // Istruiamo il finto database: "Se ti chiedo se esiste l'utente 1, rispondi di sì"
        when(utenteRepository.existsById(idUtente)).thenReturn(true);

        utenteService.eliminaAccount(idUtente);
        // Verifichiamo che l'ordine di eliminazione sia stato inviato al database
        verify(utenteRepository, times(1)).deleteById(idUtente);
    }
    @Test
    @DisplayName("Eliminazione account: Errore (Utente non trovato)")
    void testEliminaAccountNonTrovato() {
        Long idUtente = 99L;
        String messaggioErroreAtteso = "L'utente con id " + idUtente + " non esiste";

        when(utenteRepository.existsById(idUtente)).thenReturn(false);
        when(messageLang.getMessage("utente.notexist", idUtente)).thenReturn(messaggioErroreAtteso);

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            utenteService.eliminaAccount(idUtente);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        verify(utenteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Recupero password: Blocco per richiesta troppo recente (meno di 24h)")
    void testRecuperoPasswordTroppoPresto() {
        String email = "mario.rossi@email.it";
        Utente utenteDb = mock(Utente.class);
        // Supponiamo che l'utente provi a recuperare la password dopo 2 ore dall'ultima volta
        when(utenteDb.getUltimoRecuperoPassword()).thenReturn(java.time.LocalDateTime.now().minusHours(2));
        when(utenteRepository.findByEmail(email)).thenReturn(Optional.of(utenteDb));
        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            utenteService.inviaEmailRecuperoPassword(email);
        });
        assertEquals("Hai già richiesto un cambio password di recente. Riprova tra 24 ore.", eccezione.getMessage());
        //ci assicuriamo che Keycloak non sia mai stato contattato
        verifyNoInteractions(keycloak);
    }

    @Test
    @DisplayName("Recupero password: Caso di successo con invio email via Keycloak")
    void testRecuperoPasswordSuccesso() {
        String email = "mario.rossi@email.it";
        Utente utenteDb = mock(Utente.class);

        //Simuliamo che l'utente non abbia mai richiesto un recupero in passato
        when(utenteDb.getUltimoRecuperoPassword()).thenReturn(null);
        when(utenteRepository.findByEmail(email)).thenReturn(Optional.of(utenteDb));

        var realm = mock(org.keycloak.admin.client.resource.RealmResource.class);
        var UsersList = mock(org.keycloak.admin.client.resource.UsersResource.class);
        var User = mock(org.keycloak.admin.client.resource.UserResource.class);
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setId("codice-segreto-123");

        when(keycloak.realm("enterprise-realm")).thenReturn(realm);
        when(realm.users()).thenReturn(UsersList);
        when(UsersList.searchByEmail(email, true)).thenReturn(java.util.List.of(keycloakUser));
        when(UsersList.get("codice-segreto-123")).thenReturn(User);
        utenteService.inviaEmailRecuperoPassword(email);

        verify(User, times(1)).executeActionsEmail(java.util.List.of("UPDATE_PASSWORD"));
        verify(utenteDb, times(1)).setUltimoRecuperoPassword(any(java.time.LocalDateTime.class));
        verify(utenteRepository, times(1)).save(utenteDb);
    }

    @Test
    @DisplayName("Recupero password: Errore (Email non presente nel database)")
    void testRecuperoPasswordEmailNonTrovata() {
        String email = "sconosciuta@email.it";
        String messaggioErroreAtteso = "Impossibile accedere: l'indirizzo email manca nel token di autenticazione.";

        when(utenteRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(messageLang.getMessage("auth.keycloak.email_not_found", email)).thenReturn(messaggioErroreAtteso);

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            utenteService.inviaEmailRecuperoPassword(email);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        verifyNoInteractions(keycloak);
    }

    @Test
    @DisplayName("Ricerca utenti con filtri: Caso di successo")
    void testRicercaUtentiSuccesso() {
        int paginaRichiesta = 0;
        UtenteFilter filtro = new UtenteFilter();
        Utente utenteMock = mock(Utente.class);
        UtenteDTO dtoMock = new UtenteDTO();
        // Creiamo una finta "pagina" contenente un singolo utente
        Page<Utente> paginaSimulata = new PageImpl<>(List.of(utenteMock));

        // Istruiamo la ricerca per restituire la nostra pagina simulata
        when(utenteRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(paginaSimulata);

        when(modelMapper.map(utenteMock, UtenteDTO.class)).thenReturn(dtoMock);

        Page<UtenteDTO> risultato = utenteService.ricercaUtenti(filtro, paginaRichiesta);

        assertNotNull(risultato);
        assertEquals(1, risultato.getTotalElements());
    }

    @Test
    @DisplayName("Ricerca utenti con filtri: Errore (Pagina non valida)")
    @SuppressWarnings("unchecked")
    void testRicercaUtentiPaginaErrata() {
        int paginaRichiesta = 5; // Chiediamo una pagina molto avanti
        UtenteFilter filtro = new UtenteFilter();
        String messaggioErroreAtteso = "Numero di pagina non valido.";
        // Simuliamo che nel database ci siano in totale solo 2 pagine
        Page<Utente> paginaSimulata = mock(Page.class);
        when(paginaSimulata.getTotalPages()).thenReturn(2);

        when(utenteRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(paginaSimulata);

        when(messageLang.getMessage("utente.invalid_page")).thenReturn(messaggioErroreAtteso);

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            utenteService.ricercaUtenti(filtro, paginaRichiesta);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
    }

}