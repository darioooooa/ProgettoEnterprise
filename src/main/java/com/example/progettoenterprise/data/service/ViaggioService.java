package com.example.progettoenterprise.data.service;


import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.dto.ViaggioDTO;

import java.util.List;
import java.util.Map;

public interface ViaggioService {
    ViaggioDTO creaViaggio(ViaggioDTO viaggiodto, Long organizzatoreId);
    void eliminaViaggio(Long id, Long organizzatoreId);

    Map<String, Object> getStatisticheRecensioni(Long viaggioId);

    List<ViaggioDTO> ricercaFiltrata(ViaggioSpecification.ViaggioFilter viaggioFilter, Long UtenteId);
}