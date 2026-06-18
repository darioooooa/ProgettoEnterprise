package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Recensione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.RecensioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.RecensioneSpecification;
import com.example.progettoenterprise.dto.RecensioneDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecensioneServiceImplTest {

    @Mock private RecensioneRepository recensioneRepository;
    @Mock private ViaggioRepository viaggioRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private PrenotazioneRepository prenotazioneRepository;
    @Mock private MessageLang messageLang;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private RecensioneServiceImpl recensioneService;

    private Utente utenteMock;
    private Viaggio viaggioMock;
    private Recensione recensioneMock;

    @BeforeEach
    void setUp() {
        // Creazione gusci vuoti (niente lenient)
        utenteMock = mock(Utente.class);
        viaggioMock = mock(Viaggio.class);
        recensioneMock = new Recensione();
        recensioneMock.setId(100L);
        recensioneMock.setViaggio(viaggioMock);
        recensioneMock.setUtente(utenteMock);
    }


    @Test
    @DisplayName("Aggiungi Recensione: Successo")
    void testAggiungiRecensione_Successo() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(prenotazioneRepository.existsByViaggiatoreIdAndViaggioIdAndStato(1L, 10L, Prenotazione.StatoPrenotazione.CONFERMATA)).thenReturn(true);
        when(recensioneRepository.existsByViaggioIdAndUtenteId(10L, 1L)).thenReturn(false);
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().minusDays(5)); // Viaggio già iniziato

        when(recensioneRepository.save(any(Recensione.class))).thenReturn(recensioneMock);
        when(modelMapper.map(any(), eq(RecensioneDTO.class))).thenReturn(new RecensioneDTO());

        RecensioneDTO risultato = recensioneService.aggiungiRecensione(1L, 10L, 5, "Ottimo!");

        assertNotNull(risultato);
        verify(recensioneRepository, times(1)).save(any(Recensione.class));
        verify(viaggioRepository, times(1)).aggiornaStatisticheRecensione(10L, 5);
    }

    @Test
    @DisplayName("Aggiungi Recensione: Errore Voto Invalido")
    void testAggiungiRecensione_ErroreVoto() {
        when(messageLang.getMessage("recensione.invalid_rating")).thenReturn("Voto non valido");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                recensioneService.aggiungiRecensione(1L, 10L, 6, "Troppo alto!")); // 6 non è valido (1-5)

        assertEquals("Voto non valido", eccezione.getMessage());
    }

    @Test
    @DisplayName("Aggiungi Recensione: Errore Utente Senza Prenotazione")
    void testAggiungiRecensione_ErroreSenzaPrenotazione() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(prenotazioneRepository.existsByViaggiatoreIdAndViaggioIdAndStato(1L, 10L, Prenotazione.StatoPrenotazione.CONFERMATA)).thenReturn(false);
        when(messageLang.getMessage("recensione.unauthorized_prenotazione")).thenReturn("Devi viaggiare prima di recensire");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                recensioneService.aggiungiRecensione(1L, 10L, 5, "Commento"));

        assertEquals("Devi viaggiare prima di recensire", eccezione.getMessage());
    }

    @Test
    @DisplayName("Aggiungi Recensione: Errore Recensione Già Esistente")
    void testAggiungiRecensione_ErroreGiaEsistente() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(prenotazioneRepository.existsByViaggiatoreIdAndViaggioIdAndStato(1L, 10L, Prenotazione.StatoPrenotazione.CONFERMATA)).thenReturn(true);
        when(recensioneRepository.existsByViaggioIdAndUtenteId(10L, 1L)).thenReturn(true); // Già inserita!
        when(messageLang.getMessage("recensione.already_exists")).thenReturn("Hai già recensito");

        assertThrows(IllegalArgumentException.class, () ->
                recensioneService.aggiungiRecensione(1L, 10L, 5, "Commento"));
    }

    @Test
    @DisplayName("Aggiungi Recensione: Errore Viaggio Non Iniziato")
    void testAggiungiRecensione_ErroreNonIniziato() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(prenotazioneRepository.existsByViaggiatoreIdAndViaggioIdAndStato(1L, 10L, Prenotazione.StatoPrenotazione.CONFERMATA)).thenReturn(true);
        when(recensioneRepository.existsByViaggioIdAndUtenteId(10L, 1L)).thenReturn(false);

        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().plusDays(5)); // Viaggio nel futuro
        when(messageLang.getMessage("recensione.not_yet_started")).thenReturn("Viaggio non iniziato");

        assertThrows(IllegalArgumentException.class, () ->
                recensioneService.aggiungiRecensione(1L, 10L, 5, "Commento"));
    }

    @Test
    @DisplayName("Aggiungi Recensione: Entità non trovate")
    void testAggiungiRecensione_EntitaNonTrovate() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> recensioneService.aggiungiRecensione(1L, 10L, 5, "C"));

        when(utenteRepository.findById(2L)).thenReturn(Optional.of(utenteMock));
        when(prenotazioneRepository.existsByViaggiatoreIdAndViaggioIdAndStato(2L, 10L, Prenotazione.StatoPrenotazione.CONFERMATA)).thenReturn(true);
        when(recensioneRepository.existsByViaggioIdAndUtenteId(10L, 2L)).thenReturn(false);
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", 10L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> recensioneService.aggiungiRecensione(2L, 10L, 5, "C"));
    }

    @Test
    @DisplayName("Elimina Recensione: Successo (Come Autore)")
    void testEliminaRecensione_SuccessoAutore() {
        when(recensioneRepository.findById(100L)).thenReturn(Optional.of(recensioneMock));
        when(viaggioMock.getId()).thenReturn(10L);
        when(utenteMock.getId()).thenReturn(1L); // L'utente che cancella è l'autore (ID 1)
        recensioneMock.setVoto(4);

        Utente orgMock = mock(Utente.class);
        when(orgMock.getId()).thenReturn(99L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);

        recensioneService.eliminaRecensione(10L, 100L, 1L);

        verify(recensioneRepository, times(1)).delete(recensioneMock);
        verify(viaggioRepository, times(1)).ricalcolaMediaPerEliminazione(10L, 4);
    }

    @Test
    @DisplayName("Elimina Recensione: Errori Vari (Non trovata, Viaggio Sbagliato, Non Autorizzato)")
    void testEliminaRecensione_Errori() {
        // Non trovata
        when(recensioneRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("recensione.notexist", 100L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> recensioneService.eliminaRecensione(10L, 100L, 1L));

        // Viaggio sbagliato
        when(recensioneRepository.findById(100L)).thenReturn(Optional.of(recensioneMock));
        when(viaggioMock.getId()).thenReturn(10L);
        when(messageLang.getMessage("recensione.not_part_of_viaggio")).thenReturn("Err");
        assertThrows(IllegalArgumentException.class, () -> recensioneService.eliminaRecensione(99L, 100L, 1L));

        // Non Autorizzato (Non è autore, non è organizzatore, non è admin)
        when(utenteMock.getId()).thenReturn(2L); // Autore ID 2
        Utente orgMock = mock(Utente.class);
        when(orgMock.getId()).thenReturn(3L); // Org ID 3
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        Utente intrusoMock = mock(Utente.class);
        when(utenteRepository.findById(99L)).thenReturn(Optional.of(intrusoMock)); // Intruso ID 99
        when(intrusoMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);
        when(messageLang.getMessage("recensione.unauthorized_utente")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () -> recensioneService.eliminaRecensione(10L, 100L, 99L));
    }

    @Test
    @DisplayName("Aggiorna Recensione: Successo (Con ricalcolo media)")
    void testAggiornaRecensione_Successo() {
        when(recensioneRepository.findById(100L)).thenReturn(Optional.of(recensioneMock));
        when(utenteMock.getId()).thenReturn(1L);
        when(viaggioMock.getId()).thenReturn(10L);
        recensioneMock.setVoto(3); // Voto vecchio

        when(recensioneRepository.save(recensioneMock)).thenReturn(recensioneMock);
        when(modelMapper.map(any(), eq(RecensioneDTO.class))).thenReturn(new RecensioneDTO());

        // L'utente aggiorna il voto da 3 a 5
        RecensioneDTO risultato = recensioneService.aggiornaRecensione(10L, 100L, 1L, 5, "Molto meglio!");

        assertNotNull(risultato);
        verify(viaggioRepository, times(1)).ricalcolaMediaPerModifica(10L, 3, 5); // Deve ricalcolare la media
    }

    @Test
    @DisplayName("Aggiorna Recensione: Successo (Senza ricalcolo media perché il voto è uguale)")
    void testAggiornaRecensione_Successo_StessoVoto() {
        when(recensioneRepository.findById(100L)).thenReturn(Optional.of(recensioneMock));
        when(utenteMock.getId()).thenReturn(1L);
        when(viaggioMock.getId()).thenReturn(10L);
        recensioneMock.setVoto(4); // Voto vecchio

        when(recensioneRepository.save(recensioneMock)).thenReturn(recensioneMock);
        when(modelMapper.map(any(), eq(RecensioneDTO.class))).thenReturn(new RecensioneDTO());

        // L'utente cambia solo il commento, il voto resta 4
        recensioneService.aggiornaRecensione(10L, 100L, 1L, 4, "Modifico solo il testo");

        verify(viaggioRepository, never()).ricalcolaMediaPerModifica(anyLong(), anyInt(), anyInt()); // Nessun ricalcolo
    }

    @Test
    @DisplayName("Ricerca Filtrata: Successo (Viaggiatore forza l'ID del viaggio)")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_Successo() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE); // Non è admin, quindi la ricerca va limitata

        Page<Recensione> pageMock = new PageImpl<>(List.of(recensioneMock));
        when(recensioneRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageMock);
        when(modelMapper.map(any(), eq(RecensioneDTO.class))).thenReturn(new RecensioneDTO());

        RecensioneSpecification.RecensioneFilter filtro = new RecensioneSpecification.RecensioneFilter();
        Page<RecensioneDTO> risultati = recensioneService.ricercaFiltrata(filtro, 1L, 10L, 0);

        assertNotNull(risultati);
        assertEquals(1, risultati.getTotalElements());
        assertEquals(10L, filtro.getViaggioId()); // Il servizio deve aver forzato l'ID del viaggio nel filtro
    }

    @Test
    @DisplayName("Ricerca Filtrata: Errore Pagina Invalida")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_PaginaInvalida() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_ADMIN);

        Page<Recensione> pageMock = mock(Page.class);
        when(pageMock.getTotalPages()).thenReturn(2);

        when(recensioneRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageMock);
        when(messageLang.getMessage("recensione.invalid_page")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () ->
                recensioneService.ricercaFiltrata(new RecensioneSpecification.RecensioneFilter(), 1L, 10L, 5));
    }
}