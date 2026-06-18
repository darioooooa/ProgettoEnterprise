package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.ImmagineViaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ImmagineViaggioRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.dto.ImmagineViaggioDTO;
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
public class ImmagineViaggioServiceImplTest {

    @Mock private ViaggioRepository viaggioRepository;
    @Mock private ImmagineViaggioRepository immagineRepository;
    @Mock private MessageLang messageLang;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private ImmagineViaggioServiceImpl immagineService;

    private Viaggio viaggioMock;
    private Utente orgMock;
    private ImmagineViaggio immagineMock;

    // Un ID finto di Google Drive
    private final String VALID_DRIVE_URL = "https://drive.google.com/file/d/12345678901234567890123456789012/view";

    @BeforeEach
    void setUp() {
        orgMock = mock(Utente.class);
        viaggioMock = mock(Viaggio.class);
        immagineMock = new ImmagineViaggio();
        immagineMock.setId(100L);
        immagineMock.setViaggio(viaggioMock);
    }

    @Test
    @DisplayName("Aggiungi Immagine: Successo (Link Diretto Creato)")
    void testAggiungiImmagine_Successo() {
        when(immagineRepository.countByViaggioId(10L)).thenReturn(5L);
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));

        when(immagineRepository.save(any(ImmagineViaggio.class))).thenReturn(immagineMock);
        when(modelMapper.map(immagineMock, ImmagineViaggioDTO.class)).thenReturn(new ImmagineViaggioDTO());

        ImmagineViaggioDTO risultato = immagineService.aggiungiImmagine(10L, VALID_DRIVE_URL, true, 1L);

        assertNotNull(risultato);
        verify(immagineRepository).save(any(ImmagineViaggio.class));
    }

    @Test
    @DisplayName("Aggiungi Immagine: Errore URL Non Valido (Fallisce il Regex)")
    void testAggiungiImmagine_ErroreUrl() {
        String urlInvalido = "https://google.com/immagine.jpg";
        when(messageLang.getMessage("immagine.invalid_url")).thenReturn("URL non valido");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                immagineService.aggiungiImmagine(10L, urlInvalido, true, 1L));

        assertEquals("URL non valido", eccezione.getMessage());
        verify(immagineRepository, never()).save(any());
    }

    @Test
    @DisplayName("Aggiungi Immagine: Errore Limite Raggiunto (> 20)")
    void testAggiungiImmagine_ErroreLimite() {
        when(immagineRepository.countByViaggioId(10L)).thenReturn(20L);
        when(messageLang.getMessage("immagine.max_reached", 20)).thenReturn("Limite superato");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                immagineService.aggiungiImmagine(10L, VALID_DRIVE_URL, true, 1L));

        assertEquals("Limite superato", eccezione.getMessage());
    }

    @Test
    @DisplayName("Aggiungi Immagine: Errore Viaggio Non Trovato")
    void testAggiungiImmagine_ErroreViaggio() {
        when(immagineRepository.countByViaggioId(10L)).thenReturn(5L);
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", 10L)).thenReturn("Viaggio Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                immagineService.aggiungiImmagine(10L, VALID_DRIVE_URL, true, 1L));

        assertEquals("Viaggio Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Elimina Immagine: Successo")
    void testEliminaImmagine_Successo() {
        when(viaggioMock.getId()).thenReturn(10L);
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        when(immagineRepository.findById(100L)).thenReturn(Optional.of(immagineMock));

        immagineService.eliminaImmagine(10L, 100L, 1L);
        verify(immagineRepository).deleteById(100L);
    }

    @Test
    @DisplayName("Elimina Immagine: Errore Immagine Non Trovata")
    void testEliminaImmagine_ErroreNonTrovata() {
        when(immagineRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("immagine.notexist", 100L)).thenReturn("Immagine Inesistente");

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () ->
                immagineService.eliminaImmagine(10L, 100L, 1L));

        assertEquals("Immagine Inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Elimina Immagine: Errore Viaggio Sbagliato")
    void testEliminaImmagine_ErroreViaggioSbagliato() {
        when(viaggioMock.getId()).thenReturn(10L); // Appartiene al viaggio 10
        when(immagineRepository.findById(100L)).thenReturn(Optional.of(immagineMock));
        when(messageLang.getMessage("immagine.not_part_of_viaggio")).thenReturn("Immagine non di questo viaggio");

        // Proviamo a eliminarla passando viaggioId 99
        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                immagineService.eliminaImmagine(99L, 100L, 1L));

        assertEquals("Immagine non di questo viaggio", eccezione.getMessage());
    }


    @Test
    @DisplayName("Modifica Visibilità: Successo")
    void testModificaVisibilita_Successo() {
        when(viaggioMock.getId()).thenReturn(10L);
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);

        when(immagineRepository.findById(100L)).thenReturn(Optional.of(immagineMock));
        when(immagineRepository.save(immagineMock)).thenReturn(immagineMock);
        when(modelMapper.map(immagineMock, ImmagineViaggioDTO.class)).thenReturn(new ImmagineViaggioDTO());

        ImmagineViaggioDTO risultato = immagineService.modificaVisibilita(10L, 100L, false, 1L);

        assertNotNull(risultato);
        assertFalse(immagineMock.isPubblica()); // Verifica che sia stato aggiornato a false
        verify(immagineRepository).save(immagineMock);
    }

    @Test
    @DisplayName("Modifica Visibilità: Vari Errori")
    void testModificaVisibilita_Errori() {
        when(immagineRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("immagine.notexist", 100L)).thenReturn("Err");
        assertThrows(EntityNotFoundException.class, () ->
                immagineService.modificaVisibilita(10L, 100L, false, 1L));

        when(viaggioMock.getId()).thenReturn(10L);
        when(immagineRepository.findById(100L)).thenReturn(Optional.of(immagineMock));
        when(messageLang.getMessage("immagine.not_part_of_viaggio")).thenReturn("Err");
        assertThrows(IllegalArgumentException.class, () ->
                immagineService.modificaVisibilita(99L, 100L, false, 1L));

        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(messageLang.getMessage("immagine.unauthorized_utente")).thenReturn("Err");
        assertThrows(IllegalArgumentException.class, () ->
                immagineService.modificaVisibilita(10L, 100L, false, 99L));
    }

    @Test
    @DisplayName("Get Galleria: Successo (Vede tutte le immagini perché è Organizzatore)")
    void testGetGalleria_Organizzatore() {
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));

        when(immagineRepository.findByViaggioId(10L)).thenReturn(List.of(immagineMock));
        when(modelMapper.map(any(), eq(ImmagineViaggioDTO.class))).thenReturn(new ImmagineViaggioDTO());

        List<ImmagineViaggioDTO> risultato = immagineService.getGalleriaViaggio(10L, 1L);

        assertEquals(1, risultato.size());
        verify(immagineRepository, times(1)).findByViaggioId(10L);
        verify(immagineRepository, never()).findByViaggioIdAndPubblicaTrue(any());
    }

    @Test
    @DisplayName("Get Galleria: Successo (Vede solo pubbliche perché NON è Organizzatore)")
    void testGetGalleria_Viaggiatore() {
        when(orgMock.getId()).thenReturn(1L);
        when(viaggioMock.getOrganizzatore()).thenReturn(orgMock);
        when(viaggioRepository.findById(10L)).thenReturn(Optional.of(viaggioMock));

        when(immagineRepository.findByViaggioIdAndPubblicaTrue(10L)).thenReturn(List.of(immagineMock));
        when(modelMapper.map(any(), eq(ImmagineViaggioDTO.class))).thenReturn(new ImmagineViaggioDTO());

        List<ImmagineViaggioDTO> risultato = immagineService.getGalleriaViaggio(10L, 2L);

        assertEquals(1, risultato.size());
        verify(immagineRepository, times(1)).findByViaggioIdAndPubblicaTrue(10L);
        verify(immagineRepository, never()).findByViaggioId(any());
    }

    @Test
    @DisplayName("Get Galleria: Errore Viaggio Non Trovato")
    void testGetGalleria_ErroreViaggio() {
        when(viaggioRepository.findById(10L)).thenReturn(Optional.empty());
        when(messageLang.getMessage("viaggio.notexist", 10L)).thenReturn("Err");

        assertThrows(EntityNotFoundException.class, () -> immagineService.getGalleriaViaggio(10L, 1L));
    }
}