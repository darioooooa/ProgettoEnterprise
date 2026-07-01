package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggiatoreRepository;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Usiamo Mockito per gestire le annotazioni @Mock
@ExtendWith(MockitoExtension.class)
public class ViaggiatoreServiceImplTest {

    @Mock
    private ViaggiatoreRepository viaggiatoreRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageLang messageLang;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private Keycloak keycloak;

    @Mock
    private OrganizzatoreRepository organizzatoreRepository;

    @Mock
    private RichiestaPromozioneRepository richiestaPromozioneRepository;

    @InjectMocks
    private ViaggiatoreServiceImpl viaggiatoreService;

    @Test
    @DisplayName("Recupero profilo viaggiatore: Caso di successo")
    void testGetProfiloViaggiatore_Successo() {
        Long idViaggiatore = 1L;

        // Creiamo un mock per il test
        Viaggiatore viaggiatoreMock = mock(Viaggiatore.class);
        ViaggiatoreDTO dtoAtteso = new ViaggiatoreDTO();
        dtoAtteso.setId(idViaggiatore);

        // Quando cercano il viaggiatore con id=1 ritornano il mock
        when(viaggiatoreRepository.findById(idViaggiatore)).thenReturn(Optional.of(viaggiatoreMock));
        // Quando chiedono di mappare il mock restituiamo il dtoAtteso
        when(modelMapper.map(viaggiatoreMock, ViaggiatoreDTO.class)).thenReturn(dtoAtteso);

        ViaggiatoreDTO risultato = viaggiatoreService.getProfiloViaggiatore(idViaggiatore);

        assertNotNull(risultato);
        assertEquals(idViaggiatore, risultato.getId());
        verify(viaggiatoreRepository, times(1)).findById(idViaggiatore);
    }

    @Test
    @DisplayName("Recupero profilo viaggiatore: Errore (Viaggiatore non trovato)")
    void testGetProfiloViaggiatore_NonTrovato() {
        Long idViaggiatore = 99L;
        String messaggioErroreAtteso = "L'utente con id " + idViaggiatore + " non esiste";

        // Se ti chiedono l'ID 99 rispondiamo che è vuoto
        when(viaggiatoreRepository.findById(idViaggiatore)).thenReturn(Optional.empty());
        // Istruiamo il traduttore dei messaggi
        when(messageLang.getMessage("utente.notexist", idViaggiatore)).thenReturn(messaggioErroreAtteso);

        // Controlliamo che l'esecuzione del metodo scateni esattamente l'errore
        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggiatoreService.getProfiloViaggiatore(idViaggiatore);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("Aggiornamento profilo: Caso di successo")
    void testAggiornaProfilo_Successo() {
        Long idViaggiatore = 1L;
        ViaggiatoreDTO datiInviati = new ViaggiatoreDTO();
        datiInviati.setNome("Marco");
        datiInviati.setCognome("Verdi");

        Viaggiatore viaggiatoreDalDb = mock(Viaggiatore.class);
        Viaggiatore viaggiatoreSalvato = mock(Viaggiatore.class);
        ViaggiatoreDTO dtoRisposta = new ViaggiatoreDTO();
        dtoRisposta.setNome("Marco");

        // Istruiamo il finto database e il convertitore
        when(viaggiatoreRepository.findById(idViaggiatore)).thenReturn(Optional.of(viaggiatoreDalDb));
        when(viaggiatoreRepository.save(viaggiatoreDalDb)).thenReturn(viaggiatoreSalvato);
        when(modelMapper.map(viaggiatoreSalvato, ViaggiatoreDTO.class)).thenReturn(dtoRisposta);

        ViaggiatoreDTO risultato = viaggiatoreService.aggiornaProfilo(idViaggiatore, datiInviati);

        assertNotNull(risultato);
        // Controlliamo che il sistema abbia provato a cambiare nome e cognome
        verify(viaggiatoreDalDb).setNome("Marco");
        verify(viaggiatoreDalDb).setCognome("Verdi");
        // Controlliamo che il salvataggio sia stato lanciato una volta
        verify(viaggiatoreRepository, times(1)).save(viaggiatoreDalDb);
    }

    @Test
    @DisplayName("Ricerca viaggiatori: Caso di successo")
    void testCercaViaggiatori() {
        String query = "marco";
        Viaggiatore viaggiatoreMock = mock(Viaggiatore.class);
        ViaggiatoreDTO dtoMock = new ViaggiatoreDTO();

        when(viaggiatoreRepository.findByUsernameContainingIgnoreCase(query)).thenReturn(List.of(viaggiatoreMock));
        when(modelMapper.map(viaggiatoreMock, ViaggiatoreDTO.class)).thenReturn(dtoMock);

        List<ViaggiatoreDTO> risultati = viaggiatoreService.cercaViaggiatori(query);

        assertFalse(risultati.isEmpty());
        assertEquals(1, risultati.size());
    }

    @Test
    @DisplayName("Creazione richiesta promozione: Caso di successo")
    void testCreaRichiesta_Successo() {
        Long idViaggiatore = 1L;
        Viaggiatore utenteAttuale = new Viaggiatore();
        utenteAttuale.setUsername("vecchioUsername");
        utenteAttuale.setEmail("vecchia@email.it");

        RichiestaPromozioneDTO dtoInviato = new RichiestaPromozioneDTO();
        dtoInviato.setUsernameRichiesto("nuovoOrganizzatore");
        dtoInviato.setEmailProfessionale("lavoro@email.it");

        // L'utente esiste
        when(viaggiatoreRepository.findById(idViaggiatore)).thenReturn(Optional.of(utenteAttuale));

        // Simulo che i controlli di sicurezza vadano tutti a buon fine (restituiscono false)
        when(richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(idViaggiatore, RichiestaPromozione.StatoRichiesta.IN_ATTESA)).thenReturn(false);
        when(richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(idViaggiatore, RichiestaPromozione.StatoRichiesta.APPROVATA)).thenReturn(false);
        when(richiestaPromozioneRepository.existsByUsernameRichiestoAndStatoNot(dtoInviato.getUsernameRichiesto(), RichiestaPromozione.StatoRichiesta.RIFIUTATA)).thenReturn(false);
        when(utenteRepository.existsByUsername(dtoInviato.getUsernameRichiesto())).thenReturn(false);
        when(richiestaPromozioneRepository.existsByEmailProfessionaleAndStatoNot(dtoInviato.getEmailProfessionale(), RichiestaPromozione.StatoRichiesta.RIFIUTATA)).thenReturn(false);
        when(utenteRepository.existsByEmail(dtoInviato.getEmailProfessionale())).thenReturn(false);

        RichiestaPromozione richiestaSalvata = mock(RichiestaPromozione.class);
        RichiestaPromozioneDTO dtoAtteso = new RichiestaPromozioneDTO();

        when(richiestaPromozioneRepository.save(any(RichiestaPromozione.class))).thenReturn(richiestaSalvata);
        when(modelMapper.map(richiestaSalvata, RichiestaPromozioneDTO.class)).thenReturn(dtoAtteso);

        RichiestaPromozioneDTO risultato = viaggiatoreService.creaRichiestaPromozione(idViaggiatore, dtoInviato, null);

        assertNotNull(risultato);
        verify(richiestaPromozioneRepository, times(1)).save(any(RichiestaPromozione.class));
    }

    @Test
    @DisplayName("Creazione richiesta: Errore (Già in attesa di valutazione)")
    void testCreaRichiesta_ErroreGiaInAttesa() {
        Long idViaggiatore = 1L;
        Viaggiatore utenteAttuale = mock(Viaggiatore.class);
        RichiestaPromozioneDTO dtoInviato = new RichiestaPromozioneDTO();

        when(viaggiatoreRepository.findById(idViaggiatore)).thenReturn(Optional.of(utenteAttuale));
        // Simuliamo che il sistema trovi già una richiesta in stato IN_ATTESA
        when(richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(idViaggiatore, RichiestaPromozione.StatoRichiesta.IN_ATTESA)).thenReturn(true);

        ResponseStatusException eccezione = assertThrows(ResponseStatusException.class, () -> {
            viaggiatoreService.creaRichiestaPromozione(idViaggiatore, dtoInviato,null);
        });

        assertEquals(HttpStatus.CONFLICT, eccezione.getStatusCode());
        assertTrue(eccezione.getReason().contains("Hai già una richiesta in fase di valutazione"));
    }

    @Test
    @DisplayName("Trova richiesta pendente: Caso di successo")
    void testTrovaRichiestaPendente_Successo() {
        Long idViaggiatore = 1L;
        RichiestaPromozione richiestaPendente = new RichiestaPromozione();

        when(richiestaPromozioneRepository.findByViaggiatoreIdAndStato(idViaggiatore, RichiestaPromozione.StatoRichiesta.IN_ATTESA))
                .thenReturn(Optional.of(richiestaPendente));

        RichiestaPromozione risultato = viaggiatoreService.trovaRichiestaPendente(idViaggiatore);

        assertNotNull(risultato);
    }
}