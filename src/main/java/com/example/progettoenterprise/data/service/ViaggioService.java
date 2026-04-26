package com.example.progettoenterprise.data.service;


import com.example.progettoenterprise.dto.ViaggioDTO;

import java.util.List;
import java.util.Map;

public interface ViaggioService {
    ViaggioDTO creaViaggio(ViaggioDTO viaggiodto, String organizzatoreUsername);
    List<ViaggioDTO> getViaggiPerOrganizzatore(Long organizzatoreId);
    void eliminaViaggio(Long id, Long organizzatoreId);

    Map<String, Object> getStatisticheRecensioni(Long viaggioId);
}