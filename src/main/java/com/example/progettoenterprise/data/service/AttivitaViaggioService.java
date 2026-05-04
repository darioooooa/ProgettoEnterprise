package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.AttivitaViaggioDTO;

import java.util.List;

public interface AttivitaViaggioService {
    AttivitaViaggioDTO creaAttivita(AttivitaViaggioDTO attivitaViaggioDTO);
    AttivitaViaggioDTO getAttivitaById(Long id);
    List<AttivitaViaggioDTO> getTimelineSpostamenti(Long viaggioId);
    // Cerca per titolo dentro un viaggio specifico
    List<AttivitaViaggioDTO> cercaInViaggio(Long viaggioId, String keyword);
    // Filtra per prezzo massimo (Low Cost)
    List<AttivitaViaggioDTO> filtraPerBudget(Long viaggioId, Double budgetMax);
    AttivitaViaggioDTO modificaAttivitaViaggio(Long id,AttivitaViaggioDTO dto);
    void eliminaAttivitaViaggio(Long idAttivita,Long idViaggio);
}
