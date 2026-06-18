package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.ListaViaggioKey;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ItinerarioPreferitoRepository;
import com.example.progettoenterprise.data.repositories.ListaViaggioRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.dto.ListaViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListaViaggioServiceImplTest {

    @Mock private ListaViaggioRepository listaViaggioRepository;
    @Mock private ViaggioRepository viaggioRepository;
    @Mock private ItinerarioPreferitoRepository itinerarioPreferitoRepository;
    @Mock private MessageLang messageLang;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private ListaViaggioServiceImpl listaViaggioService;

    private Viaggio viaggioMock;
    private ItinerarioPreferito itinerarioMock;
    private ListaViaggio associazioneMock;

    @BeforeEach
    void setUp() {
        // Creiamo solo le sagome vuote, senza usare lenient() o when()
        viaggioMock = mock(Viaggio.class);
        itinerarioMock = mock(ItinerarioPreferito.class);
        associazioneMock = new ListaViaggio();
    }

    // =========================================================================
    // AGGIUNGI ITINERARIO AL VIAGGIO
    // =========================================================================

    @Test
    @DisplayName("Aggiungi Itinerario al Viaggio: Successo")
    void testAggiungiItinerarioAlViaggio_Successo() {
        Long idViaggio = 10L;
        Long idItinerario = 5L;

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioMock));
        when(itinerarioPreferitoRepository.findById(idItinerario)).thenReturn(Optional.of(itinerarioMock));

        when(listaViaggioRepository.save(any(ListaViaggio.class))).thenReturn(associazioneMock);

        // Usiamo any() per indicare a Mockito di accettare qualsiasi input in fase di trasformazione
        when(modelMapper.map(any(), eq(ListaViaggioDTO.class))).thenReturn(new ListaViaggioDTO());

        ListaViaggioDTO risultato = listaViaggioService.aggiungiItinerarioAlViaggio(idViaggio, idItinerario);

        assertNotNull(risultato);
        verify(listaViaggioRepository, times(1)).save(any(ListaViaggio.class));
    }

    @Test
    @DisplayName("Aggiungi Itinerario al Viaggio: Errore Viaggio Non Trovato")
    void testAggiungiItinerarioAlViaggio_ErroreViaggio() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                listaViaggioService.aggiungiItinerarioAlViaggio(10L, 5L));

        assertEquals("Viaggio non trovato", eccezione.getMessage());
        verify(listaViaggioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Aggiungi Itinerario al Viaggio: Errore Itinerario Non Trovato")
    void testAggiungiItinerarioAlViaggio_ErroreItinerario() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(itinerarioPreferitoRepository.findById(5L)).thenReturn(Optional.empty());

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                listaViaggioService.aggiungiItinerarioAlViaggio(10L, 5L));

        assertEquals("Itinerario non trovato", eccezione.getMessage());
    }

    // =========================================================================
    // GET PROGRAMMA COMPLETO
    // =========================================================================

    @Test
    @DisplayName("Get Programma Completo: Successo")
    void testGetProgrammaCompleto_Successo() {
        Long idViaggio = 10L;

        when(viaggioRepository.existsById(idViaggio)).thenReturn(true);
        when(listaViaggioRepository.findByViaggio_Id(idViaggio)).thenReturn(List.of(associazioneMock));
        when(modelMapper.map(any(), eq(ListaViaggioDTO.class))).thenReturn(new ListaViaggioDTO());

        List<ListaViaggioDTO> risultati = listaViaggioService.getProgrammaCompleto(idViaggio);

        assertFalse(risultati.isEmpty());
        assertEquals(1, risultati.size());
    }

    @Test
    @DisplayName("Get Programma Completo: Errore Viaggio Non Esiste")
    void testGetProgrammaCompleto_ErroreViaggio() {
        Long idViaggio = 10L;

        when(viaggioRepository.existsById(idViaggio)).thenReturn(false);
        when(messageLang.getMessage("viaggio.notexist", idViaggio)).thenReturn("Viaggio inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                listaViaggioService.getProgrammaCompleto(idViaggio));

        assertEquals("Viaggio inesistente", eccezione.getMessage());
    }

    // =========================================================================
    // RIMUOVI ITINERARIO DAL VIAGGIO
    // =========================================================================

    @Test
    @DisplayName("Rimuovi Itinerario dal Viaggio: Successo")
    void testRimuoviItinerarioDalViaggio_Successo() {
        Long idViaggio = 10L;
        Long idItinerario = 5L;

        // Simuliamo che la chiave esista nel DB
        when(listaViaggioRepository.existsById(any(ListaViaggioKey.class))).thenReturn(true);

        listaViaggioService.rimuoviItinerarioDalViaggio(idViaggio, idItinerario);

        verify(listaViaggioRepository, times(1)).deleteById(any(ListaViaggioKey.class));
    }

    @Test
    @DisplayName("Rimuovi Itinerario dal Viaggio: Errore Associazione Non Esiste")
    void testRimuoviItinerarioDalViaggio_ErroreNonEsiste() {
        Long idViaggio = 10L;
        Long idItinerario = 5L;

        // Simuliamo che la chiave NON esista
        when(listaViaggioRepository.existsById(any(ListaViaggioKey.class))).thenReturn(false);
        when(messageLang.getMessage("lista.notexist", idViaggio + " - " + idItinerario)).thenReturn("Associazione inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                listaViaggioService.rimuoviItinerarioDalViaggio(idViaggio, idItinerario));

        assertEquals("Associazione inesistente", eccezione.getMessage());
        verify(listaViaggioRepository, never()).deleteById(any());
    }

    // =========================================================================
    // CERCA ITINERARI SOTTO BUDGET
    // =========================================================================

    @Test
    @DisplayName("Cerca Itinerari Sotto Budget: Trovati")
    void testCercaItinerariSottoBudget_Trovati() {
        Double budget = 500.0;

        when(listaViaggioRepository.findByViaggioPrezzoLessThan(budget)).thenReturn(List.of(associazioneMock));
        when(modelMapper.map(any(), eq(ListaViaggioDTO.class))).thenReturn(new ListaViaggioDTO());

        List<ListaViaggioDTO> risultati = listaViaggioService.cercaItinerariSottoBudget(budget);

        assertFalse(risultati.isEmpty());
        assertEquals(1, risultati.size());
    }

    @Test
    @DisplayName("Cerca Itinerari Sotto Budget: Nessun Risultato (Lista Vuota)")
    void testCercaItinerariSottoBudget_Vuoto() {
        Double budget = 100.0;

        when(listaViaggioRepository.findByViaggioPrezzoLessThan(budget)).thenReturn(List.of());

        List<ListaViaggioDTO> risultati = listaViaggioService.cercaItinerariSottoBudget(budget);

        assertTrue(risultati.isEmpty());
        verify(modelMapper, never()).map(any(), any()); // Non deve tentare di mappare nulla
    }
}