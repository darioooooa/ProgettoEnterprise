package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.service.NotificaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromemoriaServiceImplTest {

    @Mock
    private PrenotazioneRepository prenotazioneRepository;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private NotificaService notificaService;

    @InjectMocks
    private PromemoriaServiceImpl promemoriaService;

    private Prenotazione prenotazioneMock;
    private Viaggio viaggioMock;
    private Utente viaggiatoreMock;

    @BeforeEach
    void setUp() {
        prenotazioneMock = mock(Prenotazione.class);
        viaggioMock = mock(Viaggio.class);
        viaggiatoreMock = mock(Utente.class);
    }

    @Test
    @DisplayName("Nessuna partenza: Il sistema si ferma senza errori")
    void testInviaPromemoria_NessunaPrenotazione() {
        // Diciamo al sistema che la ricerca non trova niente
        when(prenotazioneRepository.findPrenotazioniPerReminder(any(LocalDate.class), any(LocalDate.class), eq(Prenotazione.StatoPrenotazione.CONFERMATA)))
                .thenReturn(Collections.emptyList());

        promemoriaService.inviaPromemoriaPartenze();

        // Verifichiamo che non venga MAI inviata un'email
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
        verify(notificaService, never()).inviaNotifica(anyLong(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Avviso plurale: Mancano più giorni alla partenza")
    void testInviaPromemoria_Successo_PiuGiorni() {
        // Impostiamo la data di partenza a 5 giorni da oggi
        LocalDate traCinqueGiorni = LocalDate.now().plusDays(5);

        when(viaggiatoreMock.getId()).thenReturn(1L);
        when(viaggiatoreMock.getNome()).thenReturn("Mario");
        when(viaggiatoreMock.getEmail()).thenReturn("mario@email.it");

        when(viaggioMock.getDestinazione()).thenReturn("Parigi");
        when(viaggioMock.getDataInizio()).thenReturn(traCinqueGiorni);

        when(prenotazioneMock.getId()).thenReturn(100L);
        when(prenotazioneMock.getViaggiatore()).thenReturn(viaggiatoreMock);
        when(prenotazioneMock.getViaggio()).thenReturn(viaggioMock);

        when(prenotazioneRepository.findPrenotazioniPerReminder(any(LocalDate.class), any(LocalDate.class), eq(Prenotazione.StatoPrenotazione.CONFERMATA)))
                .thenReturn(List.of(prenotazioneMock));

        promemoriaService.inviaPromemoriaPartenze();

        // Controlliamo che l'email parta e che il testo contenga la parola "giorni" (plurale)
        verify(emailService, times(1)).sendSimpleEmail(
                eq("mario@email.it"),
                eq("Promemoria Partenza"),
                contains("tra 5 giorni!")
        );

        // Controlliamo che venga generato anche l'avviso interno
        verify(notificaService, times(1)).inviaNotifica(
                eq(1L),
                contains("Mancano 5 giorni"),
                eq(100L)
        );
    }

    @Test
    @DisplayName("Avviso singolare: Manca un giorno esatto alla partenza")
    void testInviaPromemoria_Successo_UnGiorno() {
        // Impostiamo la data di partenza a 1 giorno da oggi
        LocalDate domani = LocalDate.now().plusDays(1);

        when(viaggiatoreMock.getId()).thenReturn(1L);
        when(viaggiatoreMock.getNome()).thenReturn("Luca");
        when(viaggiatoreMock.getEmail()).thenReturn("luca@email.it");

        when(viaggioMock.getDestinazione()).thenReturn("Roma");
        when(viaggioMock.getDataInizio()).thenReturn(domani);

        when(prenotazioneMock.getId()).thenReturn(101L);
        when(prenotazioneMock.getViaggiatore()).thenReturn(viaggiatoreMock);
        when(prenotazioneMock.getViaggio()).thenReturn(viaggioMock);

        when(prenotazioneRepository.findPrenotazioniPerReminder(any(LocalDate.class), any(LocalDate.class), eq(Prenotazione.StatoPrenotazione.CONFERMATA)))
                .thenReturn(List.of(prenotazioneMock));

        promemoriaService.inviaPromemoriaPartenze();

        // Controlliamo che il testo usi la parola "giorno" (singolare)
        verify(emailService, times(1)).sendSimpleEmail(
                eq("luca@email.it"),
                anyString(),
                contains("tra 1 giorno!")
        );
    }

    @Test
    @DisplayName("Errore di invio: Il sistema cattura l'imprevisto e prosegue")
    void testInviaPromemoria_CatturaErrore() {
        LocalDate domani = LocalDate.now().plusDays(1);

        when(viaggiatoreMock.getEmail()).thenReturn("errore@email.it");
        when(viaggioMock.getDataInizio()).thenReturn(domani);
        when(prenotazioneMock.getViaggiatore()).thenReturn(viaggiatoreMock);
        when(prenotazioneMock.getViaggio()).thenReturn(viaggioMock);

        when(prenotazioneRepository.findPrenotazioniPerReminder(any(LocalDate.class), any(LocalDate.class), eq(Prenotazione.StatoPrenotazione.CONFERMATA)))
                .thenReturn(List.of(prenotazioneMock));

        // Diciamo al sistema di posta di bloccarsi improvvisamente
        doThrow(new RuntimeException("Problema di rete improvviso")).when(emailService)
                .sendSimpleEmail(anyString(), anyString(), anyString());

        // Verifichiamo che il problema venga assorbito senza far crollare tutto
        assertDoesNotThrow(() -> promemoriaService.inviaPromemoriaPartenze());
    }
}