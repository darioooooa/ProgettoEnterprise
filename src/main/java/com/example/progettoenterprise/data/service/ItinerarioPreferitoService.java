package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
import java.util.List;

public interface ItinerarioPreferitoService {
    ItinerarioPreferitoDTO creaLista(ItinerarioPreferitoDTO dto, Long proprietarioId);
    ItinerarioPreferitoDTO getListaById(Long id);
    List<ItinerarioPreferitoDTO> getMieListe(Long proprietarioId);
    List<ItinerarioPreferitoDTO> getListeCondiviseConMe(Long utenteId);
    List<ItinerarioPreferitoDTO> cercaListePubbliche(String nome);
    void aggiungiViaggioAllaLista(Long idLista, Long idViaggio, Long idUtente);
    void rimuoviViaggioDallaLista(Long idLista, Long idViaggio, Long idUtente);
    void eliminaLista(Long id, Long utenteId);
    ItinerarioPreferitoDTO cambiaVisibilita(Long id, String visibilita, Long utenteId);
}