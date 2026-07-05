package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ListaUtente;
import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ItinerarioPreferitoRepository;
import com.example.progettoenterprise.data.repositories.ListaUtenteRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItinerarioPreferitoServiceImplTest {

    @Mock private ItinerarioPreferitoRepository itinerarioRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private ViaggioRepository viaggioRepository;
    @Mock private ListaUtenteRepository listaUtenteRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private MessageLang messageLang;

    @InjectMocks
    private ItinerarioPreferitoServiceImpl itinerarioService;

    private Utente proprietarioMock;
    private ItinerarioPreferito listaMock;
    private Viaggio viaggioMock;

    @BeforeEach
    void setUp() {
        proprietarioMock = mock(Utente.class);
        viaggioMock = mock(Viaggio.class);

        listaMock = new ItinerarioPreferito();
        listaMock.setId(10L);
        listaMock.setNome("La mia lista");
        listaMock.setProprietario(proprietarioMock);
        listaMock.setContenuti(new HashSet<>());
        listaMock.setUtentiAutorizzati(new HashSet<>());
    }

    @Test
    @DisplayName("Crea Lista: Successo")
    void testCreaLista_Successo() {
        ItinerarioPreferitoDTO dto = new ItinerarioPreferitoDTO();
        dto.setNome("Nuova Lista");

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(proprietarioMock));
        when(itinerarioRepository.save(any(ItinerarioPreferito.class))).thenReturn(listaMock);
        when(modelMapper.map(listaMock, ItinerarioPreferitoDTO.class)).thenReturn(dto);

        assertNotNull(itinerarioService.creaLista(dto, 1L));
        verify(itinerarioRepository, times(1)).save(any(ItinerarioPreferito.class));
    }

    @Test
    @DisplayName("Crea Lista: Errore Proprietario Non Trovato")
    void testCreaLista_ErroreProprietario() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Utente inesistente");

        assertThrows(EntityNotFoundException.class, () -> itinerarioService.creaLista(new ItinerarioPreferitoDTO(), 1L));
    }

    @Test
    @DisplayName("Recupero Liste")
    void testLetturaListe() {
        when(itinerarioRepository.findByProprietarioId(1L)).thenReturn(List.of(listaMock));
        when(itinerarioRepository.findByNomeContainingIgnoreCaseAndVisibilita("Mia", ItinerarioPreferito.Visibilita.PUBBLICA))
                .thenReturn(List.of(listaMock));
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));

        when(listaUtenteRepository.existsByListaIdAndStato(anyLong(), any())).thenReturn(false);

        ListaUtente fintoInvito = new ListaUtente();
        fintoInvito.setLista(listaMock);
        when(listaUtenteRepository.findByUtenteIdAndStato(2L, ListaUtente.StatoInvito.ACCETTATO)).thenReturn(List.of(fintoInvito));

        when(modelMapper.map(listaMock, ItinerarioPreferitoDTO.class)).thenReturn(new ItinerarioPreferitoDTO());

        assertFalse(itinerarioService.getMieListe(1L).isEmpty());
        assertFalse(itinerarioService.cercaListePubbliche("Mia").isEmpty());
        assertNotNull(itinerarioService.getListaById(10L));
        assertFalse(itinerarioService.getListeCondiviseConMe(2L).isEmpty());
    }

    @Test
    @DisplayName("Recupero Lista: Errore Lista Non Trovata")
    void testGetListaById_Errore() {
        when(itinerarioRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Lista inesistente");
        assertThrows(EntityNotFoundException.class, () -> itinerarioService.getListaById(99L));
    }

    @Test
    @DisplayName("Cambia Visibilità: Successo")
    void testCambiaVisibilita_Successo() {
        when(proprietarioMock.getId()).thenReturn(1L);
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));
        when(itinerarioRepository.save(listaMock)).thenReturn(listaMock);
        when(modelMapper.map(listaMock, ItinerarioPreferitoDTO.class)).thenReturn(new ItinerarioPreferitoDTO());

        assertNotNull(itinerarioService.cambiaVisibilita(10L, "PUBBLICA", 1L));
        assertEquals(ItinerarioPreferito.Visibilita.PUBBLICA, listaMock.getVisibilita());
    }

    @Test
    @DisplayName("Cambia Visibilità: Errore Parola Non Valida")
    void testCambiaVisibilita_ErroreParolaInvalida() {
        when(proprietarioMock.getId()).thenReturn(1L);
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                itinerarioService.cambiaVisibilita(10L, "SEGRETA", 1L));

        assertTrue(eccezione.getMessage().contains("Visibilità non valida"));
    }

    @Test
    @DisplayName("Elimina Lista: Successo ed Errori")
    void testEliminaLista() {
        when(proprietarioMock.getId()).thenReturn(1L);
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));

        when(messageLang.getMessage("lista.unauthorized")).thenReturn("Non autorizzato");
        assertThrows(IllegalArgumentException.class, () -> itinerarioService.eliminaLista(10L, 99L));

        itinerarioService.eliminaLista(10L, 1L);
        verify(itinerarioRepository).delete(listaMock);
    }

    @Test
    @DisplayName("Aggiungi Viaggio: Successo")
    void testAggiungiViaggio_Successo() {
        when(proprietarioMock.getId()).thenReturn(1L);

        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));
        when(viaggioRepository.findById(50L)).thenReturn(Optional.of(viaggioMock));

        itinerarioService.aggiungiViaggioAllaLista(10L, 50L, 1L);

        assertEquals(1, listaMock.getContenuti().size());
        verify(itinerarioRepository).save(listaMock);
    }

    @Test
    @DisplayName("Aggiungi Viaggio: Errore Viaggio Già Presente")
    void testAggiungiViaggio_GiaPresente() {
        when(proprietarioMock.getId()).thenReturn(1L);
        when(viaggioMock.getId()).thenReturn(50L); // Qui serve perché la lista ha già un elemento!

        ListaViaggio collegamento = new ListaViaggio();
        collegamento.setViaggio(viaggioMock);
        listaMock.getContenuti().add(collegamento);

        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));
        when(viaggioRepository.findById(50L)).thenReturn(Optional.of(viaggioMock));

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                itinerarioService.aggiungiViaggioAllaLista(10L, 50L, 1L));

        assertTrue(eccezione.getMessage().contains("già presente"));
    }

    @Test
    @DisplayName("Aggiungi Viaggio: Vari Errori")
    void testAggiungiViaggio_Errori() {
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Lista inesistente");
        assertThrows(EntityNotFoundException.class, () -> itinerarioService.aggiungiViaggioAllaLista(10L, 50L, 1L));

        when(proprietarioMock.getId()).thenReturn(1L);
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));
        when(messageLang.getMessage("itinerario.unauthorized")).thenReturn("Non autorizzato");
        assertThrows(IllegalArgumentException.class, () -> itinerarioService.aggiungiViaggioAllaLista(10L, 50L, 99L));

        when(viaggioRepository.findById(50L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itinerarioService.aggiungiViaggioAllaLista(10L, 50L, 1L));
    }

    @Test
    @DisplayName("Rimuovi Viaggio: Successo")
    void testRimuoviViaggio_Successo() {
        when(proprietarioMock.getId()).thenReturn(1L);
        when(viaggioMock.getId()).thenReturn(50L); // Anche qui serve, rimuove un elemento esistente

        ListaViaggio collegamento = new ListaViaggio();
        collegamento.setViaggio(viaggioMock);
        listaMock.getContenuti().add(collegamento);

        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));

        itinerarioService.rimuoviViaggioDallaLista(10L, 50L, 1L);

        assertTrue(listaMock.getContenuti().isEmpty());
        verify(itinerarioRepository).save(listaMock);
    }

    @Test
    @DisplayName("Rimuovi Viaggio: Errore Viaggio Non Presente")
    void testRimuoviViaggio_NonPresente() {
        when(proprietarioMock.getId()).thenReturn(1L);
        // Tolta la finta domanda sull'ID

        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                itinerarioService.rimuoviViaggioDallaLista(10L, 50L, 1L));

        assertTrue(eccezione.getMessage().contains("non è presente"));
    }

    @Test
    @DisplayName("Rimuovi Viaggio: Vari Errori")
    void testRimuoviViaggio_Errori() {
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> itinerarioService.rimuoviViaggioDallaLista(10L, 50L, 1L));

        when(proprietarioMock.getId()).thenReturn(1L);
        when(itinerarioRepository.findById(10L)).thenReturn(Optional.of(listaMock));
        when(messageLang.getMessage("itinerario.unauthorized")).thenReturn("Err");
        assertThrows(IllegalArgumentException.class, () -> itinerarioService.rimuoviViaggioDallaLista(10L, 50L, 99L));
    }
}