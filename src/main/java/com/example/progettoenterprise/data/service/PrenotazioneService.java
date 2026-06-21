package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.PrenotazioneSpecification;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface PrenotazioneService {
    PrenotazioneDTO creaPrenotazione(Long idPrenotazione, Long idUtente, Integer numeroPersone);
    void cancellaPrenotazione(Long id,Long idUtente);
    PrenotazioneDTO getPrenotazioneById(Long id, Long utenteId);
    byte[] esportaPrenotazioni(Long idPrenotazione);
    Page<PrenotazioneDTO> ricercaFiltrata(PrenotazioneSpecification.PrenotazioneFilter prenotazioneFilter, Long utenteId, int page);
    Optional<PrenotazioneDTO> ottieniStatoPrenotazioneUtente(Long viaggioId, Long utenteId);
}
