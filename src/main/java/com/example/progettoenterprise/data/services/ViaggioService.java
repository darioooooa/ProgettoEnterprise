package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.dto.ViaggioDTO;

import java.util.List;

public interface ViaggioService {
    Viaggio creaViaggio(ViaggioDTO dto, Long organizzatoreId);
    List<Viaggio> getViaggiPerOrganizzatore(Long organizzatoreId);
    void eliminaViaggio(Long id, Long organizzatoreId);
}