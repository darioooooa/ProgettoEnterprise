package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ImmagineViaggioDTO;

import java.util.List;

public interface ImmagineViaggioService {
    ImmagineViaggioDTO aggiungiImmagine(Long viaggioId, String url, boolean pubblica, Long organizzatoreId);
    void eliminaImmagine(Long viaggioId, Long immagineId, Long organizzatoreId);
    ImmagineViaggioDTO modificaVisibilita(Long viaggioId, Long immagineId, boolean nuovaVisibilita, Long organizzatoreId);
    List<ImmagineViaggioDTO> getGalleriaViaggio(Long viaggioId, Long utenteId);
}
