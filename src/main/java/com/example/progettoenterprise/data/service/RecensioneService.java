package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.RecensioneDTO;

import java.util.List;

public interface RecensioneService {
    RecensioneDTO aggiungiRecensione(String username, Long viaggioId, int voto, String commento);
    void eliminaRecensione(Long viaggioId, Long recensioneId, String username);
    RecensioneDTO aggiornaRecensione(Long viaggioId, Long recensioneId, String username, int nuovoVoto, String nuovoCommento);

    List<RecensioneDTO> getRecensioniViaggio(Long viaggioId);
}
