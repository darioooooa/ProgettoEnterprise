package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.RecensioneSpecification;
import com.example.progettoenterprise.dto.RecensioneDTO;
import org.springframework.data.domain.Page;

public interface RecensioneService {
    RecensioneDTO aggiungiRecensione(Long utenteId, Long viaggioId, int voto, String commento);
    void eliminaRecensione(Long viaggioId, Long recensioneId, Long utenteId);
    RecensioneDTO aggiornaRecensione(Long viaggioId, Long recensioneId, Long utenteId, int nuovoVoto, String nuovoCommento);
    Page<RecensioneDTO> ricercaFiltrata(RecensioneSpecification.RecensioneFilter recensioneFilter, Long utenteId, Long viaggioId, int page);
}
