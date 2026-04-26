package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.AttivitaViaggioDTO;

public interface AttivitaViaggioService {
    AttivitaViaggioDTO creaAttivita(AttivitaViaggioDTO attivitaViaggioDTO);
    AttivitaViaggioDTO getAttivitaById(Long id);
    AttivitaViaggioDTO modificaAttivitaViaggio(Long id,AttivitaViaggioDTO dto);
    void eliminaAttivitaViaggio(Long id);
}
