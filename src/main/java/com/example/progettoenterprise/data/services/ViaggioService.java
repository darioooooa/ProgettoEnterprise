package com.example.progettoenterprise.data.services;


import com.example.progettoenterprise.dto.ViaggioDTO;

import java.util.List;

public interface ViaggioService {
    ViaggioDTO creaViaggio(ViaggioDTO viaggiodto, Long organizzatoreId);
    List<ViaggioDTO> getViaggiPerOrganizzatore(Long organizzatoreId);
    void eliminaViaggio(Long id, Long organizzatoreId);
}