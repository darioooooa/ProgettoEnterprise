package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.PrenotazioneSpecification;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrenotazioneServiceImplTest {

    @Mock private ViaggioRepository viaggioRepository;
    @Mock private PrenotazioneRepository prenotazioneRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ModelMapper modelMapper;
    @Mock private MessageLang messageLang;

    @InjectMocks
    private PrenotazioneServiceImpl prenotazioneService;

    private Viaggio viaggioMock;
    private Utente utenteMock;
    private Prenotazione prenotazioneMock;

    @BeforeEach
    void setUp() {

        viaggioMock = mock(Viaggio.class);
        utenteMock = mock(Utente.class);
        prenotazioneMock = mock(Prenotazione.class);
    }

    @Test
    @DisplayName("Crea Prenotazione: Successo")
    void testCreaPrenotazione_Successo() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().plusDays(10)); // Data futura
        when(viaggioMock.getDataFine()).thenReturn(LocalDate.now().plusDays(15));
        when(prenotazioneRepository.findPrenotazioniSovrapposte(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(viaggioMock.getPartecipantiAttuali()).thenReturn(5);
        when(viaggioMock.getMaxPartecipanti()).thenReturn(20);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(prenotazioneRepository.save(any(Prenotazione.class))).thenReturn(new Prenotazione());
        when(modelMapper.map(any(), eq(PrenotazioneDTO.class))).thenReturn(new PrenotazioneDTO());

        PrenotazioneDTO risultato = prenotazioneService.creaPrenotazione(10L, 1L, 2);

        assertNotNull(risultato);
        verify(viaggioRepository, times(1)).save(viaggioMock);
        verify(prenotazioneRepository, times(1)).save(any(Prenotazione.class));
    }

    @Test
    @DisplayName("Crea Prenotazione: Errore Viaggio Non Trovato")
    void testCreaPrenotazione_ErroreViaggio() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", 10L)).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> prenotazioneService.creaPrenotazione(10L, 1L, 2));
    }

    @Test
    @DisplayName("Crea Prenotazione: Errore Viaggio Scaduto")
    void testCreaPrenotazione_ErroreScaduto() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().minusDays(5)); // Data passata
        when(messageLang.getMessage("prenotazione.viaggio.scaduto")).thenReturn("Err");

        assertThrows(IllegalStateException.class, () -> prenotazioneService.creaPrenotazione(10L, 1L, 2));
    }

    @Test
    @DisplayName("Crea Prenotazione: Errore Sovrapposizione")
    void testCreaPrenotazione_ErroreSovrapposizione() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().plusDays(10));
        when(viaggioMock.getDataFine()).thenReturn(LocalDate.now().plusDays(15));

        // Simuliamo la presenza di una prenotazione già esistente per quelle date
        when(prenotazioneRepository.findPrenotazioniSovrapposte(eq(1L), any(), any()))
                .thenReturn(List.of(new Prenotazione()));

        assertThrows(IllegalStateException.class, () -> prenotazioneService.creaPrenotazione(10L, 1L, 2));
    }

    @Test
    @DisplayName("Crea Prenotazione: Errore Posti Esauriti")
    void testCreaPrenotazione_ErrorePosti() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().plusDays(10));
        when(viaggioMock.getDataFine()).thenReturn(LocalDate.now().plusDays(15));
        when(prenotazioneRepository.findPrenotazioniSovrapposte(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        // 20 attuali + 5 richiesti = 25 (maggiore del max 20)
        when(viaggioMock.getPartecipantiAttuali()).thenReturn(20);
        when(viaggioMock.getMaxPartecipanti()).thenReturn(20);

        assertThrows(IllegalStateException.class, () -> prenotazioneService.creaPrenotazione(10L, 1L, 5));
    }

    @Test
    @DisplayName("Crea Prenotazione: Errore Utente Non Trovato")
    void testCreaPrenotazione_ErroreUtente() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now().plusDays(10));
        when(viaggioMock.getDataFine()).thenReturn(LocalDate.now().plusDays(15));
        when(prenotazioneRepository.findPrenotazioniSovrapposte(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(viaggioMock.getPartecipantiAttuali()).thenReturn(5);
        when(viaggioMock.getMaxPartecipanti()).thenReturn(20);

        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> prenotazioneService.creaPrenotazione(10L, 1L, 2));
    }


    @Test
    @DisplayName("Cancella Prenotazione: Successo")
    void testCancellaPrenotazione_Successo() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneMock));
        when(prenotazioneMock.getViaggiatore()).thenReturn(utenteMock);
        when(utenteMock.getId()).thenReturn(1L);
        when(prenotazioneMock.getStato()).thenReturn(Prenotazione.StatoPrenotazione.IN_ATTESA);
        when(prenotazioneMock.getViaggio()).thenReturn(viaggioMock);
        when(prenotazioneMock.getNumeroPersone()).thenReturn(2);
        when(viaggioMock.getPartecipantiAttuali()).thenReturn(5);

        // Elementi per preparare le info della notifica
        Utente organizzatore = mock(Utente.class);
        when(viaggioMock.getOrganizzatore()).thenReturn(organizzatore);
        when(organizzatore.getFirebaseToken()).thenReturn("token-organizzatore");
        when(utenteMock.getUsername()).thenReturn("utente1");
        when(viaggioMock.getDestinazione()).thenReturn("Parigi");

        prenotazioneService.cancellaPrenotazione(100L, 1L);

        verify(viaggioMock).setPartecipantiAttuali(3); // 5 - 2
        verify(viaggioRepository).save(viaggioMock);
        verify(prenotazioneRepository).delete(prenotazioneMock);
    }

    @Test
    @DisplayName("Cancella Prenotazione: Non Autorizzato e Non Trovata")
    void testCancellaPrenotazione_Errori() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("prenotazione.notexist", 100L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> prenotazioneService.cancellaPrenotazione(100L, 1L));

        // Non Autorizzato
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneMock));
        when(prenotazioneMock.getViaggiatore()).thenReturn(utenteMock);
        when(utenteMock.getId()).thenReturn(2L); // Proprietario è 2, ma richiede 1
        when(messageLang.getMessage("prenotazione.unauthorized")).thenReturn("Err");
        assertThrows(IllegalArgumentException.class, () -> prenotazioneService.cancellaPrenotazione(100L, 1L));
    }

    @Test
    @DisplayName("Cancella Prenotazione: Errore Già Confermata")
    void testCancellaPrenotazione_ErroreConfermata() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneMock));
        when(prenotazioneMock.getViaggiatore()).thenReturn(utenteMock);
        when(utenteMock.getId()).thenReturn(1L);
        when(prenotazioneMock.getStato()).thenReturn(Prenotazione.StatoPrenotazione.CONFERMATA); // Errore

        assertThrows(IllegalStateException.class, () -> prenotazioneService.cancellaPrenotazione(100L, 1L));
    }


    @Test
    @DisplayName("Get Prenotazione: Successo (Admin)")
    void testGetPrenotazione_Admin() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneMock));
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_ADMIN);

        // Prepariamo i finti dati interni per accontentare il test
        Utente proprietario = mock(Utente.class);
        when(proprietario.getId()).thenReturn(2L);
        when(prenotazioneMock.getViaggiatore()).thenReturn(proprietario);
        when(prenotazioneMock.getViaggio()).thenReturn(viaggioMock);
        Utente org = mock(Utente.class);
        when(org.getId()).thenReturn(3L);
        when(viaggioMock.getOrganizzatore()).thenReturn(org);

        when(modelMapper.map(any(), eq(PrenotazioneDTO.class))).thenReturn(new PrenotazioneDTO());

        assertNotNull(prenotazioneService.getPrenotazioneById(100L, 1L));
    }

    @Test
    @DisplayName("Esporta Calendario ICS")
    void testEsportaPrenotazioni() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneMock));
        when(prenotazioneMock.getViaggio()).thenReturn(viaggioMock);
        when(viaggioMock.getDataInizio()).thenReturn(LocalDate.now());
        when(viaggioMock.getDataFine()).thenReturn(LocalDate.now().plusDays(5));
        when(viaggioMock.getId()).thenReturn(10L);
        when(viaggioMock.getTitolo()).thenReturn("Titolo Viaggio");
        when(viaggioMock.getDestinazione()).thenReturn("Roma");

        byte[] icsData = prenotazioneService.esportaPrenotazioni(100L);

        assertNotNull(icsData);
        assertTrue(icsData.length > 0);
        String icsString = new String(icsData);
        assertTrue(icsString.contains("BEGIN:VCALENDAR"));
        assertTrue(icsString.contains("SUMMARY:Viaggio: Titolo Viaggio"));
    }

    @Test
    @DisplayName("Ricerca Filtrata: Successo con tutti i Ruoli")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_Ruoli() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);

        Page<Prenotazione> pageMock = new PageImpl<>(List.of(prenotazioneMock));
        when(prenotazioneRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageMock);
        when(modelMapper.map(any(), eq(PrenotazioneDTO.class))).thenReturn(new PrenotazioneDTO());

        Page<PrenotazioneDTO> risultati = prenotazioneService.ricercaFiltrata(new PrenotazioneSpecification.PrenotazioneFilter(), 1L, 0);

        assertNotNull(risultati);
        assertEquals(1, risultati.getTotalElements());
    }

    @Test
    @DisplayName("Ricerca Filtrata: Errore Pagina Invalida")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_ErrorePagina() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_ADMIN);

        Page<Prenotazione> pageMock = mock(Page.class);
        when(pageMock.getTotalPages()).thenReturn(2);

        when(prenotazioneRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageMock);
        when(messageLang.getMessage("prenotazione.invalid_page")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () ->
                prenotazioneService.ricercaFiltrata(new PrenotazioneSpecification.PrenotazioneFilter(), 1L, 5));
    }
}