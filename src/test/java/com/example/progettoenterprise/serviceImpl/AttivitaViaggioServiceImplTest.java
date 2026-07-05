package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.AttivitaViaggioRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.AttivitaViaggioSpecification;
import com.example.progettoenterprise.dto.AttivitaViaggioDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttivitaViaggioServiceImplTest {

    @Mock private AttivitaViaggioRepository attivitaViaggioRepository;
    @Mock private ViaggioRepository viaggioRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private MessageLang messageLang;

    @InjectMocks
    private AttivitaViaggioServiceImpl attivitaService;

    private Viaggio viaggioMock;
    private Utente orgMock;
    private AttivitaViaggio attivitaMock;

    // Date fittizie per accontentare il controllore del calendario
    private final LocalDate inizioViaggio = LocalDate.of(2026, 8, 1);
    private final LocalDate fineViaggio = LocalDate.of(2026, 8, 10);
    private final LocalDateTime inizioAttivita = LocalDateTime.of(2026, 8, 5, 10, 0);
    private final LocalDateTime fineAttivita = LocalDateTime.of(2026, 8, 5, 12, 0);

    @BeforeEach
    void setUp() {
        orgMock = mock(Utente.class);
        viaggioMock = mock(Viaggio.class);
        attivitaMock = new AttivitaViaggio();
        attivitaMock.setId(100L);
        attivitaMock.setViaggio(viaggioMock);
    }

    @Test
    @DisplayName("Crea Attività: Successo")
    void testCreaAttivita_Successo() {
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(viaggioMock.getDataInizio()).thenReturn(inizioViaggio);
        when(viaggioMock.getDataFine()).thenReturn(fineViaggio);

        AttivitaViaggioDTO dto = new AttivitaViaggioDTO();
        dto.setOrarioInizio(inizioAttivita);
        dto.setOrarioFine(fineAttivita);

        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(modelMapper.map(dto, AttivitaViaggio.class)).thenReturn(attivitaMock);
        when(attivitaViaggioRepository.save(attivitaMock)).thenReturn(attivitaMock);
        when(modelMapper.map(attivitaMock, AttivitaViaggioDTO.class)).thenReturn(new AttivitaViaggioDTO());

        assertNotNull(attivitaService.creaAttivita(10L, dto, 1L));
        verify(attivitaViaggioRepository).save(attivitaMock);
    }

    @Test
    @DisplayName("Crea Attività: Errore Viaggio Non Trovato")
    void testCreaAttivita_ViaggioNonTrovato() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Errore");

        assertThrows(EntityNotFoundException.class, () -> attivitaService.creaAttivita(10L, new AttivitaViaggioDTO(), 1L));
    }

    @Test
    @DisplayName("Crea Attività: Errore Non Autorizzato")
    void testCreaAttivita_NonAutorizzato() {
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(messageLang.getMessage(anyString())).thenReturn("Errore");

        assertThrows(IllegalArgumentException.class, () -> attivitaService.creaAttivita(10L, new AttivitaViaggioDTO(), 99L));
    }

    @Test
    @DisplayName("Get Attività: Successo (Viaggiatore)")
    void testGetAttivitaById_Successo_Viaggiatore() {
        when(viaggioMock.getId()).thenReturn(10L);

        Utente viaggiatore = mock(Utente.class);

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        when(utenteRepository.findById(2L)).thenReturn(Optional.of(viaggiatore));
        when(modelMapper.map(attivitaMock, AttivitaViaggioDTO.class)).thenReturn(new AttivitaViaggioDTO());

        assertNotNull(attivitaService.getAttivitaById(100L, 10L, 2L));
    }

    @Test
    @DisplayName("Get Attività: Successo (Organizzatore corretto)")
    void testGetAttivitaById_Successo_Organizzatore() {
        when(viaggioMock.getId()).thenReturn(10L);

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(orgMock));
        when(modelMapper.map(attivitaMock, AttivitaViaggioDTO.class)).thenReturn(new AttivitaViaggioDTO());

        assertNotNull(attivitaService.getAttivitaById(100L, 10L, 1L));
    }

    @Test
    @DisplayName("Get Attività: Errore Attività o Utente non trovato")
    void testGetAttivitaById_NonTrovati() {
        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Errore");
        assertThrows(EntityNotFoundException.class, () -> attivitaService.getAttivitaById(100L, 10L, 1L));

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attivitaService.getAttivitaById(100L, 10L, 1L));
    }

    @Test
    @DisplayName("Get Attività: Errore Viaggio Sbagliato")
    void testGetAttivitaById_ViaggioSbagliato() {
        when(viaggioMock.getId()).thenReturn(10L);
        Utente u = mock(Utente.class);

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(u));
        when(messageLang.getMessage(anyString(), any())).thenReturn("Errore");

        assertThrows(EntityNotFoundException.class, () -> attivitaService.getAttivitaById(100L, 99L, 1L));
    }

    @Test
    @DisplayName("Modifica Attività: Successo")
    void testModificaAttivita_Successo() {
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(viaggioMock.getDataInizio()).thenReturn(inizioViaggio);
        when(viaggioMock.getDataFine()).thenReturn(fineViaggio);

        AttivitaViaggioDTO dto = new AttivitaViaggioDTO();
        dto.setTitolo("Nuovo Titolo");
        dto.setOrarioInizio(inizioAttivita);
        dto.setOrarioFine(fineAttivita);

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        when(attivitaViaggioRepository.save(attivitaMock)).thenReturn(attivitaMock);
        when(modelMapper.map(attivitaMock, AttivitaViaggioDTO.class)).thenReturn(new AttivitaViaggioDTO());

        assertNotNull(attivitaService.modificaAttivitaViaggio(100L, dto, 1L));
        assertEquals("Nuovo Titolo", attivitaMock.getTitolo());
    }

    @Test
    @DisplayName("Modifica Attività: Errore Non Trovato o Non Autorizzato")
    void testModificaAttivita_Errori() {
        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageLang.getMessage(anyString(), any())).thenReturn("Errore");
        assertThrows(EntityNotFoundException.class, () -> attivitaService.modificaAttivitaViaggio(100L, new AttivitaViaggioDTO(), 1L));

        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        when(messageLang.getMessage(anyString())).thenReturn("Errore");
        assertThrows(IllegalArgumentException.class, () -> attivitaService.modificaAttivitaViaggio(100L, new AttivitaViaggioDTO(), 99L));
    }

    @Test
    @DisplayName("Elimina Attività: Successo")
    void testEliminaAttivita_Successo() {
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getId()).thenReturn(10L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        attivitaService.eliminaAttivitaViaggio(100L, 10L, 1L);
        verify(attivitaViaggioRepository).delete(attivitaMock);
    }

    @Test
    @DisplayName("Elimina Attività: Errori vari")
    void testEliminaAttivita_Errori() {
        // Non trovata
        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attivitaService.eliminaAttivitaViaggio(100L, 10L, 1L));

        // Viaggio sbagliato
        when(viaggioMock.getId()).thenReturn(10L);
        when(attivitaViaggioRepository.findById(100L)).thenReturn(Optional.of(attivitaMock));
        assertThrows(IllegalArgumentException.class, () -> attivitaService.eliminaAttivitaViaggio(100L, 99L, 1L));

        // Organizzatore sbagliato
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(messageLang.getMessage(anyString())).thenReturn("Errore");
        assertThrows(IllegalArgumentException.class, () -> attivitaService.eliminaAttivitaViaggio(100L, 10L, 99L));
    }

    @Test
    @DisplayName("Ricerca Filtrata: Successo")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_Successo() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(orgMock));

        Page<AttivitaViaggio> page = new PageImpl<>(List.of(attivitaMock));
        when(attivitaViaggioRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(modelMapper.map(any(), eq(AttivitaViaggioDTO.class))).thenReturn(new AttivitaViaggioDTO());

        assertNotNull(attivitaService.ricercaFiltrata(new AttivitaViaggioSpecification.AttivitaFilter(), 10L, 1L, 0));
    }

    @Test
    @DisplayName("Ricerca Filtrata: Errori di base e Pagina Invalida")
    @SuppressWarnings("unchecked")
    void testRicercaFiltrata_Errori() {
        // Entità non trovate
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attivitaService.ricercaFiltrata(new AttivitaViaggioSpecification.AttivitaFilter(), 10L, 1L, 0));

        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> attivitaService.ricercaFiltrata(new AttivitaViaggioSpecification.AttivitaFilter(), 10L, 1L, 0));

        // Pagina invalida
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(orgMock));
        Page<AttivitaViaggio> mockPage = mock(Page.class);
        when(mockPage.getTotalPages()).thenReturn(5);
        when(attivitaViaggioRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);
        when(messageLang.getMessage("attivita.invalid_page")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () -> attivitaService.ricercaFiltrata(new AttivitaViaggioSpecification.AttivitaFilter(), 10L, 1L, 10));
    }
}