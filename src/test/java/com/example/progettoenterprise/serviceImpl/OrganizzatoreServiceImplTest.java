package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Organizzatore;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.dto.OrganizzatoreDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Diciamo a JUnit di usare Mockito per gestire le simulazioni
@ExtendWith(MockitoExtension.class)
public class OrganizzatoreServiceImplTest {

    @Mock
    private OrganizzatoreRepository organizzatoreRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageLang messageLang;

    @InjectMocks
    private OrganizzatoreServiceImpl organizzatoreService;

    @Test
    @DisplayName("Recupero profilo organizzatore: Caso di successo")
    void testGetProfilo_Successo() {
        Long idOrganizzatore = 1L;

        // Creiamo le controfigure per il test
        Organizzatore organizzatoreMock = mock(Organizzatore.class);
        OrganizzatoreDTO dtoAtteso = new OrganizzatoreDTO();
        dtoAtteso.setId(idOrganizzatore);

        // Istruiamo il finto database e il convertitore
        when(organizzatoreRepository.findById(idOrganizzatore)).thenReturn(Optional.of(organizzatoreMock));
        when(modelMapper.map(organizzatoreMock, OrganizzatoreDTO.class)).thenReturn(dtoAtteso);

        OrganizzatoreDTO risultato = organizzatoreService.getProfilo(idOrganizzatore);

        assertNotNull(risultato);
        assertEquals(idOrganizzatore, risultato.getId());
        verify(organizzatoreRepository, times(1)).findById(idOrganizzatore);
    }

    @Test
    @DisplayName("Recupero profilo organizzatore: Errore (Organizzatore non trovato)")
    void testGetProfilo_NonTrovato() {
        Long idOrganizzatore = 99L;
        String messaggioErroreAtteso = "L'organizzatore con id " + idOrganizzatore + " non esiste";

        // Simuliamo che la ricerca nel database non dia risultati
        when(organizzatoreRepository.findById(idOrganizzatore)).thenReturn(Optional.empty());
        when(messageLang.getMessage("organizzatore.notexist", idOrganizzatore)).thenReturn(messaggioErroreAtteso);

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            organizzatoreService.getProfilo(idOrganizzatore);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        // Ci assicuriamo che il sistema si sia bloccato prima di tentare la conversione
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("Aggiornamento profilo organizzatore: Caso di successo")
    void testUpdateProfilo_Successo() {
        Long idOrganizzatore = 1L;

        OrganizzatoreDTO datiInviati = new OrganizzatoreDTO();
        datiInviati.setNome("Alessandro");
        datiInviati.setCognome("Neri");

        Organizzatore organizzatoreDalDb = mock(Organizzatore.class);
        Organizzatore organizzatoreSalvato = mock(Organizzatore.class);
        OrganizzatoreDTO dtoRisposta = new OrganizzatoreDTO();
        dtoRisposta.setNome("Alessandro");

        // Prepariamo la catena di azioni
        when(organizzatoreRepository.findById(idOrganizzatore)).thenReturn(Optional.of(organizzatoreDalDb));
        when(organizzatoreRepository.save(organizzatoreDalDb)).thenReturn(organizzatoreSalvato);
        when(modelMapper.map(organizzatoreSalvato, OrganizzatoreDTO.class)).thenReturn(dtoRisposta);

        OrganizzatoreDTO risultato = organizzatoreService.updateProfilo(idOrganizzatore, datiInviati);

        assertNotNull(risultato);

        // Verifichiamo che i nuovi dati siano stati applicati correttamente all'oggetto
        verify(organizzatoreDalDb).setNome("Alessandro");
        verify(organizzatoreDalDb).setCognome("Neri");

        // Verifichiamo che il comando di salvataggio sia partito
        verify(organizzatoreRepository, times(1)).save(organizzatoreDalDb);
    }

    @Test
    @DisplayName("Aggiornamento profilo organizzatore: Errore (Organizzatore non trovato)")
    void testUpdateProfilo_NonTrovato() {
        Long idOrganizzatore = 99L;
        OrganizzatoreDTO datiInviati = new OrganizzatoreDTO();
        String messaggioErroreAtteso = "L'organizzatore con id " + idOrganizzatore + " non esiste";

        when(organizzatoreRepository.findById(idOrganizzatore)).thenReturn(Optional.empty());
        when(messageLang.getMessage("organizzatore.notexist", idOrganizzatore)).thenReturn(messaggioErroreAtteso);

        EntityNotFoundException eccezione = assertThrows(EntityNotFoundException.class, () -> {
            organizzatoreService.updateProfilo(idOrganizzatore, datiInviati);
        });

        assertEquals(messaggioErroreAtteso, eccezione.getMessage());
        // Controllo di sicurezza: verifichiamo che non sia stato salvato nulla per errore
        verify(organizzatoreRepository, never()).save(any());
    }
}