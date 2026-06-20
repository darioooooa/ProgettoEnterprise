package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Amicizia;
import com.example.progettoenterprise.data.entities.Amicizia.StatoAmicizia;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.AmiciziaRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.dto.AmiciziaDTO;
import jakarta.persistence.EntityNotFoundException;
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
public class AmiciziaServiceImplTest {
    @Mock private AmiciziaRepository amiciziaRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private MessageLang messageLang;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private AmiciziaServiceImpl amiciziaService;

    @Test
    @DisplayName("Invia Richiesta: Successo")
    void testInviaRichiesta_Successo() {
        Utente richiedente = mock(Utente.class);
        when(richiedente.getId()).thenReturn(1L);
        when(richiedente.getUsername()).thenReturn("mario");

        Utente ricevente = mock(Utente.class);
        when(ricevente.getId()).thenReturn(2L);
        when(ricevente.getUsername()).thenReturn("luigi");

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(richiedente));
        when(utenteRepository.findByUsername("luigi")).thenReturn(Optional.of(ricevente));
        when(amiciziaRepository.findQualsiasiRelazione(richiedente, ricevente)).thenReturn(Optional.empty());
        when(amiciziaRepository.save(any(Amicizia.class))).thenReturn(new Amicizia());
        when(modelMapper.map(any(), eq(AmiciziaDTO.class))).thenReturn(new AmiciziaDTO());

        assertNotNull(amiciziaService.inviaRichiesta(1L, "luigi"));
        verify(amiciziaRepository, times(1)).save(any(Amicizia.class));
    }

    @Test
    @DisplayName("Invia Richiesta: Errore Richiedente Non Trovato")
    void testInviaRichiesta_RichiedenteNonTrovato() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> amiciziaService.inviaRichiesta(1L, "luigi"));
    }

    @Test
    @DisplayName("Invia Richiesta: Errore Ricevente Non Trovato")
    void testInviaRichiesta_RiceventeNonTrovato() {
        Utente richiedente = mock(Utente.class);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(richiedente));
        when(utenteRepository.findByUsername("luigi")).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.username_notexist", "luigi")).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> amiciziaService.inviaRichiesta(1L, "luigi"));
    }

    @Test
    @DisplayName("Invia Richiesta: Errore (Richiesta a se stessi)")
    void testInviaRichiesta_SeStessi() {
        Utente utente = mock(Utente.class);
        when(utente.getId()).thenReturn(1L);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(utenteRepository.findByUsername("mario")).thenReturn(Optional.of(utente));
        when(messageLang.getMessage("amicizia.self_request")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () -> amiciziaService.inviaRichiesta(1L, "mario"));
    }

    @Test
    @DisplayName("Invia Richiesta: Errore (Relazione Già Esistente - In Attesa o Accettata)")
    void testInviaRichiesta_GiaEsistente() {
        Utente req = mock(Utente.class); when(req.getId()).thenReturn(1L);
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(2L);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(req));
        when(utenteRepository.findByUsername("luigi")).thenReturn(Optional.of(rec));

        // Creiamo un'amicizia che è già IN_ATTESA
        Amicizia esistente = new Amicizia();
        esistente.setStato(Amicizia.StatoAmicizia.IN_ATTESA);

        when(amiciziaRepository.findQualsiasiRelazione(req, rec)).thenReturn(Optional.of(esistente));
        when(messageLang.getMessage("amicizia.already_exists")).thenReturn("Err");

        assertThrows(IllegalStateException.class, () -> amiciziaService.inviaRichiesta(1L, "luigi"));
    }

    @Test
    @DisplayName("Invia Richiesta: Successo (Ricicla Amicizia Rifiutata)")
    void testInviaRichiesta_RiciclaRifiutata() {
        Utente req = mock(Utente.class); when(req.getId()).thenReturn(1L);
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(2L);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(req));
        when(utenteRepository.findByUsername("luigi")).thenReturn(Optional.of(rec));

        // Creiamo un'amicizia che in passato era stata RIFIUTATA
        Amicizia vecchiaRifiutata = new Amicizia();
        vecchiaRifiutata.setStato(Amicizia.StatoAmicizia.RIFIUTATA);

        when(amiciziaRepository.findQualsiasiRelazione(req, rec)).thenReturn(Optional.of(vecchiaRifiutata));
        when(amiciziaRepository.save(any(Amicizia.class))).thenReturn(vecchiaRifiutata);
        when(modelMapper.map(vecchiaRifiutata, AmiciziaDTO.class)).thenReturn(new AmiciziaDTO());

        assertNotNull(amiciziaService.inviaRichiesta(1L, "luigi"));

        // Verifichiamo che il programma abbia cambiato lo stato da RIFIUTATA a IN_ATTESA
        assertEquals(Amicizia.StatoAmicizia.IN_ATTESA, vecchiaRifiutata.getStato());
    }

    @Test
    @DisplayName("Accetta Richiesta: Successo")
    void testAccettaRichiesta_Successo() {
        Amicizia am = new Amicizia();
        am.setStato(StatoAmicizia.IN_ATTESA);
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(2L);
        am.setRicevente(rec);
        Utente req = mock(Utente.class); am.setRichiedente(req);

        when(amiciziaRepository.findById(1L)).thenReturn(Optional.of(am));
        when(amiciziaRepository.save(am)).thenReturn(am);
        when(modelMapper.map(any(), eq(AmiciziaDTO.class))).thenReturn(new AmiciziaDTO());

        assertNotNull(amiciziaService.accettaRichiesta(1L, 2L));
        assertEquals(StatoAmicizia.ACCETTATA, am.getStato());
    }

    @Test
    @DisplayName("Accetta Richiesta: Errore Amicizia Non Trovata")
    void testAccettaRichiesta_NonTrovata() {
        when(amiciziaRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("amicizia.notexist", 1L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.accettaRichiesta(1L, 2L));
    }

    @Test
    @DisplayName("Accetta Richiesta: Errore Non Autorizzato")
    void testAccettaRichiesta_NonAutorizzato() {
        Amicizia am = new Amicizia();
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(99L);
        am.setRicevente(rec);

        when(amiciziaRepository.findById(1L)).thenReturn(Optional.of(am));
        when(messageLang.getMessage("amicizia.unauthorized")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () -> amiciziaService.accettaRichiesta(1L, 2L));
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Successo")
    void testRifiutaRichiesta_Successo() {
        Amicizia am = new Amicizia();
        am.setStato(StatoAmicizia.IN_ATTESA);
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(2L);
        am.setRicevente(rec);

        when(amiciziaRepository.findById(1L)).thenReturn(Optional.of(am));

        amiciziaService.rifiutaRichiesta(1L, 2L);
        assertEquals(StatoAmicizia.RIFIUTATA, am.getStato());
        verify(amiciziaRepository).save(am);
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Errore Non Trovata")
    void testRifiutaRichiesta_NonTrovata() {
        when(amiciziaRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("amicizia.notexist", 1L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.rifiutaRichiesta(1L, 2L));
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Errore Non Autorizzato")
    void testRifiutaRichiesta_NonAutorizzato() {
        Amicizia am = new Amicizia();
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(99L);
        am.setRicevente(rec);

        when(amiciziaRepository.findById(1L)).thenReturn(Optional.of(am));
        when(messageLang.getMessage("amicizia.unauthorized")).thenReturn("Err");

        assertThrows(IllegalArgumentException.class, () -> amiciziaService.rifiutaRichiesta(1L, 2L));
    }

    @Test
    @DisplayName("Rifiuta Richiesta: Errore Non in Attesa")
    void testRifiutaRichiesta_NonInAttesa() {
        Amicizia am = new Amicizia();
        am.setStato(StatoAmicizia.ACCETTATA);
        Utente rec = mock(Utente.class); when(rec.getId()).thenReturn(2L);
        am.setRicevente(rec);

        when(amiciziaRepository.findById(1L)).thenReturn(Optional.of(am));
        when(messageLang.getMessage("amicizia.not_pending")).thenReturn("Err");

        assertThrows(IllegalStateException.class, () -> amiciziaService.rifiutaRichiesta(1L, 2L));
    }

    @Test
    @DisplayName("Rimuovi Amico: Successo")
    void testRimuoviAmico_Successo() {
        Utente req = mock(Utente.class); Utente rec = mock(Utente.class);
        Amicizia am = new Amicizia(); am.setStato(StatoAmicizia.ACCETTATA);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(req));
        when(utenteRepository.findById(2L)).thenReturn(Optional.of(rec));
        when(amiciziaRepository.findQualsiasiRelazione(req, rec)).thenReturn(Optional.of(am));

        amiciziaService.rimuoviAmico(1L, 2L);
        verify(amiciziaRepository).delete(am);
    }

    @Test
    @DisplayName("Rimuovi Amico: Errore Richiedente")
    void testRimuoviAmico_RichiedenteErr() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.rimuoviAmico(1L, 2L));
    }

    @Test
    @DisplayName("Rimuovi Amico: Errore Ricevente")
    void testRimuoviAmico_RiceventeErr() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(mock(Utente.class)));
        when(utenteRepository.findById(2L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 2L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.rimuoviAmico(1L, 2L));
    }

    @Test
    @DisplayName("Rimuovi Amico: Errore Relazione Inesistente")
    void testRimuoviAmico_NessunaRelazione() {
        Utente req = mock(Utente.class); Utente rec = mock(Utente.class);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(req));
        when(utenteRepository.findById(2L)).thenReturn(Optional.of(rec));
        when(amiciziaRepository.findQualsiasiRelazione(req, rec)).thenReturn(Optional.empty());
        when(messageLang.getMessage("amicizia.notexist", 1L, 2L)).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> amiciziaService.rimuoviAmico(1L, 2L));
    }
    @Test
    @DisplayName("Liste Amici: Successo")
    void testListe_Successi() {
        Utente u = mock(Utente.class);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(u));
        when(modelMapper.map(any(), eq(AmiciziaDTO.class))).thenReturn(new AmiciziaDTO());

        // Miei Amici
        when(amiciziaRepository.findAllAmiciConfermati(u)).thenReturn(List.of(new Amicizia()));
        assertEquals(1, amiciziaService.getMieiAmici(1L).size());

        // Richieste Ricevute
        when(amiciziaRepository.findByRiceventeAndStato(u, StatoAmicizia.IN_ATTESA)).thenReturn(List.of(new Amicizia()));
        assertEquals(1, amiciziaService.getRichiesteRicevute(1L).size());

        // Richieste Inviate
        when(amiciziaRepository.findByRichiedenteAndStato(u, StatoAmicizia.IN_ATTESA)).thenReturn(List.of(new Amicizia()));
        assertEquals(1, amiciziaService.getRichiesteInviate(1L).size());

        // Richieste Rifiutate
        when(amiciziaRepository.findAllRelazioniPerStato(u, StatoAmicizia.RIFIUTATA)).thenReturn(List.of(new Amicizia()));
        assertEquals(1, amiciziaService.getRichiesteRifiutate(1L).size());
    }

    @Test
    @DisplayName("Liste Amici: Errore Utente Non Trovato")
    void testListe_Errori() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("utente.notexist", 1L)).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> amiciziaService.getMieiAmici(1L));
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.getRichiesteRicevute(1L));
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.getRichiesteInviate(1L));
        assertThrows(EntityNotFoundException.class, () -> amiciziaService.getRichiesteRifiutate(1L));
    }
}
