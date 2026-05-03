package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ImmagineViaggioDTO;

import java.util.List;

public interface ImmagineViaggioService {
    ImmagineViaggioDTO aggiungiImmagine(Long viaggioId, String url, boolean pubblica, String organizzatoreUsername);
    void eliminaImmagine(Long viaggioId, Long immagineId, String organizzatoreUsername);
    ImmagineViaggioDTO modificaVisibilita(Long viaggioId, Long immagineId, boolean nuovaVisibilita, String organizzatoreUsername);
    List<ImmagineViaggioDTO> getGalleriaViaggio(Long viaggioId, String utenteUsername);
}
