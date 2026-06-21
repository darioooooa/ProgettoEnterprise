package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.data.service.PagamentoService;
import com.example.progettoenterprise.dto.AttivitaViaggioDTO;
import com.example.progettoenterprise.dto.ViaggioDTO;
import com.example.progettoenterprise.dto.ViaggioMappaDTO;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViaggioServiceImplTest {

    @Mock
    private ViaggioRepository viaggioRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private PrenotazioneRepository prenotazioneRepository;

    @Mock
    private PagamentoService pagamentoService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageLang messageLang;

    @InjectMocks
    private ViaggioServiceImpl viaggioService;


    @Test
    @DisplayName("Creazione viaggio: Caso di successo con tappe")
    void testCreaViaggio_Successo() {
        Long idOrganizzatore = 1L;
        Utente organizzatoreMock = mock(Utente.class);

        ViaggioDTO inputDto = new ViaggioDTO();
        inputDto.setPrezzo(150.0);
        inputDto.setDataInizio(LocalDate.now().plusDays(10));
        inputDto.setDataFine(LocalDate.now().plusDays(15));

        AttivitaViaggioDTO tappaDto = new AttivitaViaggioDTO();
        inputDto.setTappe(List.of(tappaDto));

        Viaggio viaggioMappato = new Viaggio();
        viaggioMappato.setPrezzo(150.0);
        AttivitaViaggio tappa = new AttivitaViaggio();
        viaggioMappato.setTappe(List.of(tappa));

        Viaggio viaggioSalvato = mock(Viaggio.class);
        ViaggioDTO dtoRisposta = new ViaggioDTO();
        dtoRisposta.setPrezzo(150.0);

        when(utenteRepository.findById(idOrganizzatore)).thenReturn(Optional.of(organizzatoreMock));
        when(modelMapper.map(inputDto, Viaggio.class)).thenReturn(viaggioMappato);
        when(viaggioRepository.save(any(Viaggio.class))).thenReturn(viaggioSalvato);
        when(modelMapper.map(viaggioSalvato, ViaggioDTO.class)).thenReturn(dtoRisposta);

        ViaggioDTO risultato = viaggioService.creaViaggio(inputDto, idOrganizzatore);

        assertNotNull(risultato);
        assertEquals(150.0, risultato.getPrezzo());
        verify(viaggioRepository, times(1)).save(viaggioMappato);
    }

    @Test
    @DisplayName("Creazione viaggio: Errore (Organizzatore non trovato)")
    void testCreaViaggio_OrganizzatoreNonTrovato() {
        Long idOrganizzatore = 99L;
        ViaggioDTO inputDto = new ViaggioDTO();
        String messaggioErroreAtteso = "L'utente con id " + idOrganizzatore + " non esiste";

        when(utenteRepository.findById(idOrganizzatore)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", idOrganizzatore)).thenReturn(messaggioErroreAtteso);

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.creaViaggio(inputDto, idOrganizzatore);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        verify(viaggioRepository, never()).save(any(Viaggio.class));
    }

    @Test
    @DisplayName("Creazione viaggio: Errore (Date non valide)")
    void testCreaViaggio_ErroreDate() {
        Long idOrganizzatore = 1L;
        Utente organizzatoreMock = mock(Utente.class);

        ViaggioDTO inputDto = new ViaggioDTO();
        Viaggio viaggioMappato = new Viaggio();
        viaggioMappato.setPrezzo(100.0);
        viaggioMappato.setDataInizio(LocalDate.now().plusDays(15));
        viaggioMappato.setDataFine(LocalDate.now().plusDays(10));

        String messaggioErroreAtteso = "Le date devono essere valide.";

        when(utenteRepository.findById(idOrganizzatore)).thenReturn(Optional.of(organizzatoreMock));
        when(modelMapper.map(inputDto, Viaggio.class)).thenReturn(viaggioMappato);
        when(messageLang.getMessage("viaggio.invalid_date")).thenReturn(messaggioErroreAtteso);

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            viaggioService.creaViaggio(inputDto, idOrganizzatore);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
    }

    @Test
    @DisplayName("Modifica viaggio: Caso di successo con tappe")
    void testModificaViaggio_Successo() {
        Long idViaggio = 1L;
        Long idOrganizzatore = 5L;

        ViaggioDTO datiInviati = new ViaggioDTO();
        datiInviati.setTitolo("Nuovo Titolo");
        datiInviati.setPrezzo(200.0);
        AttivitaViaggioDTO tappaDto = new AttivitaViaggioDTO();
        datiInviati.setTappe(List.of(tappaDto));

        Viaggio viaggioEsistente = new Viaggio();
        viaggioEsistente.setTappe(new ArrayList<>());
        Utente organizzatore = mock(Utente.class);
        when(organizzatore.getId()).thenReturn(idOrganizzatore);
        viaggioEsistente.setOrganizzatore(organizzatore);

        Viaggio viaggioAggiornato = mock(Viaggio.class);
        ViaggioDTO dtoRisposta = new ViaggioDTO();
        dtoRisposta.setTitolo("Nuovo Titolo");

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioEsistente));
        when(modelMapper.map(tappaDto, AttivitaViaggio.class)).thenReturn(new AttivitaViaggio());
        when(viaggioRepository.save(viaggioEsistente)).thenReturn(viaggioAggiornato);
        when(modelMapper.map(viaggioAggiornato, ViaggioDTO.class)).thenReturn(dtoRisposta);

        ViaggioDTO risultato = viaggioService.modificaViaggio(idViaggio, datiInviati, idOrganizzatore);

        assertNotNull(risultato);
        verify(viaggioRepository, times(1)).save(viaggioEsistente);
    }

    @Test
    @DisplayName("Modifica viaggio: Errore (Utente non autorizzato)")
    void testModificaViaggio_NonAutorizzato() {
        Long idViaggio = 1L;
        Long idProprietario = 5L;
        Long idUtenteNonAutorizzato = 9L;

        ViaggioDTO datiInviati = new ViaggioDTO();
        Viaggio viaggioEsistente = new Viaggio();

        Utente organizzatore = mock(Utente.class);
        when(organizzatore.getId()).thenReturn(idProprietario);
        viaggioEsistente.setOrganizzatore(organizzatore);

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioEsistente));
        when(messageLang.getMessage("viaggio.unauthorized")).thenReturn("Non sei autorizzato.");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            viaggioService.modificaViaggio(idViaggio, datiInviati, idUtenteNonAutorizzato);
        });

        assertEquals("Non sei autorizzato.", eccezione.getMessage());
    }

    @Test
    @DisplayName("Modifica viaggio: Errore (Non trovato)")
    void testModificaViaggio_NonTrovato() {
        Long idViaggio = 99L;
        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", idViaggio)).thenReturn("Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.modificaViaggio(idViaggio, new ViaggioDTO(), 1L);
        });

        assertEquals("Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Eliminazione viaggio: Caso di successo con annullamento e rimborsi")
    void testEliminaViaggio_Successo() throws Exception {
        Long idViaggio = 1L;
        Long idOrganizzatore = 5L;

        Viaggio viaggioDalDb = new Viaggio();
        viaggioDalDb.setId(idViaggio);

        // Essendo Utente abstract, usiamo mock()
        Utente proprietario = mock(Utente.class);
        lenient().when(proprietario.getId()).thenReturn(idOrganizzatore);
        viaggioDalDb.setOrganizzatore(proprietario);

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setId(10L);

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioDalDb));
        when(prenotazioneRepository.findByViaggioIdAndStato(idViaggio, Prenotazione.StatoPrenotazione.CONFERMATA))
                .thenReturn(List.of(prenotazione));

        viaggioService.eliminaViaggio(idViaggio, idOrganizzatore);

        verify(pagamentoService, times(1)).rimborsaPrenotazione(10L);

        // Verifica che il viaggio sia stato ANNULLATO e salvato
        assertEquals(Viaggio.StatoViaggio.ANNULLATO, viaggioDalDb.getStato());
        verify(viaggioRepository, times(1)).save(viaggioDalDb);
        verify(viaggioRepository, never()).delete((Viaggio) any());
    }

    @Test
    @DisplayName("Eliminazione viaggio: Errore (Non trovato)")
    void testEliminaViaggio_NonTrovato() {
        Long idViaggio = 99L;
        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", idViaggio)).thenReturn("Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.eliminaViaggio(idViaggio, 1L);
        });

        assertEquals("Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Recupero viaggio per ID base: Caso di successo")
    void testGetViaggioById_Base_Successo() {
        Long idViaggio = 1L;
        Viaggio viaggioMock = new Viaggio();
        viaggioMock.setDataInizio(LocalDate.now());
        viaggioMock.setDataFine(LocalDate.now().plusDays(5));

        ViaggioDTO dtoMock = new ViaggioDTO();

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioMock));
        when(modelMapper.map(viaggioMock, ViaggioDTO.class)).thenReturn(dtoMock);

        ViaggioDTO risultato = viaggioService.getViaggioById(idViaggio);

        assertNotNull(risultato);
    }

    @Test
    @DisplayName("Recupero viaggio per ID base: Errore (Non trovato)")
    void testGetViaggioById_Base_NonTrovato() {
        Long idViaggio = 99L;
        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", idViaggio)).thenReturn("Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.getViaggioById(idViaggio);
        });

        assertEquals("Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Recupero viaggio per ID con tappe: Caso di successo")
    void testGetViaggioById_ConTappe_Successo() {
        Long idViaggio = 1L;
        Long idUtente = 2L;
        Viaggio viaggioMock = mock(Viaggio.class);
        ViaggioDTO dtoMock = new ViaggioDTO();

        when(viaggioRepository.findByIdConTappe(idViaggio)).thenReturn(Optional.of(viaggioMock));
        when(modelMapper.map(viaggioMock, ViaggioDTO.class)).thenReturn(dtoMock);

        ViaggioDTO risultato = viaggioService.getViaggioById(idViaggio, idUtente);

        assertNotNull(risultato);
    }


    @Test
    @DisplayName("Ricerca viaggi per mappa (Organizzatore): Caso di successo")
    void testGetViaggiMappa_Organizzatore() {
        Long idUtente = 1L;
        Utente orgMock = mock(Utente.class);
        when(orgMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_ORGANIZZATORE);
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(orgMock));

        Viaggio v1 = new Viaggio();
        v1.setId(10L);
        v1.setLatitudine(41.9028);
        v1.setLongitudine(12.4964);

        when(viaggioRepository.findByOrganizzatoreId(idUtente)).thenReturn(List.of(v1));

        List<ViaggioMappaDTO> risultati = viaggioService.getViaggiMappa(idUtente);

        assertFalse(risultati.isEmpty());
    }

    @Test
    @DisplayName("Ricerca viaggi per mappa (Viaggiatore): Caso di successo")
    void testGetViaggiMappa_Viaggiatore() {
        Long idUtente = 1L;
        Utente viaggiatoreMock = mock(Utente.class);
        when(viaggiatoreMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(viaggiatoreMock));

        Viaggio v1 = new Viaggio();
        v1.setId(10L);

        when(viaggioRepository.findAll()).thenReturn(List.of(v1));

        List<ViaggioMappaDTO> risultati = viaggioService.getViaggiMappa(idUtente);

        assertFalse(risultati.isEmpty());
    }

    @Test
    @DisplayName("Ricerca viaggi per mappa: Errore (Utente non trovato)")
    void testGetViaggiMappa_UtenteNonTrovato() {
        Long idUtente = 99L;
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", idUtente)).thenReturn("Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.getViaggiMappa(idUtente);
        });

        assertEquals("Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Ricerca viaggi per organizzatore: Caso di successo")
    void testGetViaggiByOrganizzatore() {
        Long idOrganizzatore = 5L;
        Viaggio v1 = mock(Viaggio.class);

        when(viaggioRepository.findByOrganizzatoreId(idOrganizzatore)).thenReturn(List.of(v1));
        when(modelMapper.map(v1, ViaggioDTO.class)).thenReturn(new ViaggioDTO());

        List<ViaggioDTO> risultati = viaggioService.getViaggiByOrganizzatore(idOrganizzatore);

        assertEquals(1, risultati.size());
    }

    @Test
    @DisplayName("Ricerca filtrata: Caso di successo")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_Successo() {
        Long idUtente = 1L;
        int paginaRichiesta = 0;
        ViaggioSpecification.ViaggioFilter filtro = new ViaggioSpecification.ViaggioFilter();

        Utente utenteMock = mock(Utente.class);
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_ORGANIZZATORE);
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(utenteMock));

        Viaggio viaggioMock = mock(Viaggio.class);
        Page<Viaggio> paginaSimulata = new PageImpl<>(List.of(viaggioMock));

        when(viaggioRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(paginaSimulata);
        when(modelMapper.map(viaggioMock, ViaggioDTO.class)).thenReturn(new ViaggioDTO());

        Page<ViaggioDTO> risultato = viaggioService.ricercaFiltrata(filtro, idUtente, paginaRichiesta);

        assertNotNull(risultato);
        assertEquals(1, risultato.getTotalElements());
    }

    @Test
    @DisplayName("Ricerca filtrata: Errore (Pagina non valida)")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_ErrorePagina() {
        Long idUtente = 1L;
        int paginaRichiesta = 10;
        ViaggioSpecification.ViaggioFilter filtro = new ViaggioSpecification.ViaggioFilter();

        Utente utenteMock = mock(Utente.class);
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);
        when(utenteRepository.findById(idUtente)).thenReturn(Optional.of(utenteMock));

        Page<Viaggio> paginaSimulata = mock(Page.class);
        when(paginaSimulata.getTotalPages()).thenReturn(2);

        when(viaggioRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(paginaSimulata);
        when(messageLang.getMessage("viaggio.invalid_page")).thenReturn("Pagina non valida.");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            viaggioService.ricercaFiltrata(filtro, idUtente, paginaRichiesta);
        });

        assertEquals("Pagina non valida.", eccezione.getMessage());
    }

    @Test
    @DisplayName("Ricerca filtrata: Errore (Utente non trovato)")
    void testRicercaFiltrata_UtenteNonTrovato() {
        Long idUtente = 99L;
        ViaggioSpecification.ViaggioFilter filtro = new ViaggioSpecification.ViaggioFilter();

        when(utenteRepository.findById(idUtente)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", idUtente)).thenReturn("Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.ricercaFiltrata(filtro, idUtente, 0);
        });

        assertEquals("Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Lettura statistiche recensioni: Caso di successo")
    void testGetStatisticheRecensioni_Successo() {
        Long idViaggio = 1L;
        Viaggio viaggioMock = mock(Viaggio.class);
        Utente organizzatoreMock = mock(Utente.class);

        when(viaggioMock.getId()).thenReturn(idViaggio);
        when(viaggioMock.getDestinazione()).thenReturn("Roma");
        when(viaggioMock.getMediaRecensioni()).thenReturn(4.5);
        when(viaggioMock.getNumeroRecensioni()).thenReturn(10);
        when(viaggioMock.getOrganizzatore()).thenReturn(organizzatoreMock);
        when(organizzatoreMock.getUsername()).thenReturn("orgRoma");
        when(organizzatoreMock.getId()).thenReturn(5L);

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioMock));

        Map<String, Object> statistiche = viaggioService.getStatisticheRecensioni(idViaggio);

        assertNotNull(statistiche);
        assertEquals("Roma", statistiche.get("destinazione"));
    }

    @Test
    @DisplayName("Lettura statistiche recensioni: Errore (Non trovato)")
    void testGetStatisticheRecensioni_NonTrovato() {
        Long idViaggio = 99L;
        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", idViaggio)).thenReturn("Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            viaggioService.getStatisticheRecensioni(idViaggio);
        });

        assertEquals("Inesistente", eccezione.getMessage());
    }
}