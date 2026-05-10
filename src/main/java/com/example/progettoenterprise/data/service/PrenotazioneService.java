package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.PrenotazioneSpecification;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import java.util.List;
public interface PrenotazioneService {
    PrenotazioneDTO creaPrenotazione(Long idPrenotazione, Long idUtente, Integer numeroPersone);
    void cancellaPrenotazione(Long id,Long idUtente);
    PrenotazioneDTO getPrenotazioneById(Long id, Long utenteId);
    byte[] esportaPrenotazioni(Long viaggioId);
    List<PrenotazioneDTO> ricercaFiltrata(PrenotazioneSpecification.PrenotazioneFilter prenotazioneFilter, Long utenteId);
}
