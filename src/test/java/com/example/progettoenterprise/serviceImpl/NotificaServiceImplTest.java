package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Notifica;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.NotificaRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.specifications.NotificaSpecification;
import com.example.progettoenterprise.dto.NotificaDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificaServiceImplTest {

    @Mock private UtenteRepository utenteRepository;
    @Mock private NotificaRepository notificaRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private MessageLang messageLang;

    @InjectMocks
    private NotificaServiceImpl notificaService;

    private Utente utenteMock;
    private Notifica notificaMock;
    private NotificaSpecification.NotificaFilter filterMock;

    @BeforeEach
    void setUp() {
        utenteMock = mock(Utente.class);
        notificaMock = new Notifica();
        notificaMock.setId(10L);
        filterMock = new NotificaSpecification.NotificaFilter();
    }

    @Test
    @DisplayName("Invia Notifica: Successo")
    void testInviaNotifica_Successo() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));
        when(notificaRepository.save(any(Notifica.class))).thenReturn(notificaMock);
        when(modelMapper.map(notificaMock, NotificaDTO.class)).thenReturn(new NotificaDTO());

        NotificaDTO risultato = notificaService.inviaNotifica(1L, "Nuovo messaggio", 100L);

        assertNotNull(risultato);
        verify(notificaRepository, times(1)).save(any(Notifica.class));
    }

    @Test
    @DisplayName("Invia Notifica: Errore Messaggio Vuoto o Nullo")
    void testInviaNotifica_ErroreMessaggio() {
        when(messageLang.getMessage("notifica.messaggio.vuoto")).thenReturn("Messaggio vuoto");

        IllegalArgumentException ecc1 = assertThrows(IllegalArgumentException.class, () ->
                notificaService.inviaNotifica(1L, null, 100L));
        assertEquals("Messaggio vuoto", ecc1.getMessage());

        // Test messaggio composto solo da spazi
        IllegalArgumentException ecc2 = assertThrows(IllegalArgumentException.class, () ->
                notificaService.inviaNotifica(1L, "    ", 100L));
        assertEquals("Messaggio vuoto", ecc2.getMessage());

        verify(notificaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invia Notifica: Errore Utente Non Trovato")
    void testInviaNotifica_ErroreUtente() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Utente inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                notificaService.inviaNotifica(1L, "Messaggio", 100L));

        assertEquals("Utente inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Get Notifiche: Successo")
    @SuppressWarnings("unchecked")
    void testGetNotifiche_Successo() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));

        Page<Notifica> pageMock = new PageImpl<>(List.of(notificaMock));
        when(notificaRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageMock);
        when(modelMapper.map(any(), eq(NotificaDTO.class))).thenReturn(new NotificaDTO());

        Page<NotificaDTO> risultati = notificaService.getNotifiche(1L, filterMock, 0);

        assertNotNull(risultati);
        assertEquals(1, risultati.getTotalElements());
    }

    @Test
    @DisplayName("Get Notifiche: Errore Utente Non Trovato")
    void testGetNotifiche_ErroreUtente() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Utente inesistente");

        assertThrows(EntityNotFoundException.class, () ->
                notificaService.getNotifiche(1L, filterMock, 0));
    }

    @Test
    @DisplayName("Get Notifiche: Errore Pagina Invalida")
    @SuppressWarnings("unchecked")
    void testGetNotifiche_ErrorePagina() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utenteMock));

        Page<Notifica> pageMock = mock(Page.class);
        when(pageMock.getTotalPages()).thenReturn(2); // Diciamo che ci sono 2 pagine in totale

        when(notificaRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pageMock);
        when(messageLang.getMessage("notifica.invalid_page")).thenReturn("Pagina non valida");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                notificaService.getNotifiche(1L, filterMock, 5));

        assertEquals("Pagina non valida", eccezione.getMessage());
    }

    @Test
    @DisplayName("Segna Come Letta: Successo")
    void testSegnaComeLetta_Successo() {
        notificaMock.setLetta(false);
        when(notificaRepository.findById(10L)).thenReturn(Optional.of(notificaMock));

        notificaService.segnaComeLetta(10L);

        assertTrue(notificaMock.isLetta());
        verify(notificaRepository, times(1)).save(notificaMock);
    }

    @Test
    @DisplayName("Segna Come Letta: Errore Non Trovata")
    void testSegnaComeLetta_Errore() {
        when(notificaRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("notifica.notexist", 10L)).thenReturn("Notifica inesistente");

        assertThrows(EntityNotFoundException.class, () -> notificaService.segnaComeLetta(10L));
    }

    @Test
    @DisplayName("Segna Tutte Come Lette: Successo con elementi")
    void testSegnaTutteComeLette_ConElementi() {
        notificaMock.setLetta(false);
        when(notificaRepository.findAllByUtenteIdAndIsLettaIsFalseOrderByDataCreazioneDesc(1L))
                .thenReturn(List.of(notificaMock));

        notificaService.segnaTutteComeLette(1L);

        assertTrue(notificaMock.isLetta());
        verify(notificaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Segna Tutte Come Lette: Nessuna notifica da leggere (Ottimizzazione)")
    void testSegnaTutteComeLette_ListaVuota() {
        when(notificaRepository.findAllByUtenteIdAndIsLettaIsFalseOrderByDataCreazioneDesc(1L))
                .thenReturn(Collections.emptyList());

        notificaService.segnaTutteComeLette(1L);

        // Deve accorgersi che la lista è vuota e fare return immediato (risparmiando un salvataggio inutile)
        verify(notificaRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Conteggio Notifiche Non Lette: Successo")
    void testConteggioNotificheNonLette() {
        when(notificaRepository.countByUtenteIdAndIsLettaIsFalse(1L)).thenReturn(5L);

        long count = notificaService.conteggioNotificheNonLette(1L);

        assertEquals(5L, count);
        verify(notificaRepository, times(1)).countByUtenteIdAndIsLettaIsFalse(1L);
    }

    @Test
    @DisplayName("Elimina Notifica: Successo")
    void testEliminaNotifica_Successo() {
        when(notificaRepository.existsById(10L)).thenReturn(true);

        notificaService.eliminaNotifica(10L);

        verify(notificaRepository, times(1)).deleteById(10L);
    }

    @Test
    @DisplayName("Elimina Notifica: Errore Non Esiste")
    void testEliminaNotifica_Errore() {
        when(notificaRepository.existsById(10L)).thenReturn(false);
        when(messageLang.getMessage("notifica.notexist", 10L)).thenReturn("Notifica inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                notificaService.eliminaNotifica(10L));

        assertEquals("Notifica inesistente", eccezione.getMessage());
        verify(notificaRepository, never()).deleteById(anyLong());
    }
}