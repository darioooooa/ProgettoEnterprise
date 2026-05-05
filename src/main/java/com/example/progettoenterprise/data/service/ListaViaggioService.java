package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ListaViaggioDTO;

import java.util.List;

public interface ListaViaggioService {
    ListaViaggioDTO aggiungiItinerarioAlViaggio(Long viaggioId, Long itinerarioId);
    List<ListaViaggioDTO> getProgrammaCompleto(Long viaggioId);
    void rimuoviItinerarioDalViaggio(Long ViaggioId, Long itinerarioId);
    List<ListaViaggioDTO> cercaItinerariSottoBudget(Double Budget);
}
