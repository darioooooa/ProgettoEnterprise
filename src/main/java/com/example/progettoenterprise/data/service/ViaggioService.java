package com.example.progettoenterprise.data.service;


import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.dto.ViaggioDTO;
import com.example.progettoenterprise.dto.ViaggioMappaDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ViaggioService {
    ViaggioDTO creaViaggio(ViaggioDTO viaggiodto, Long organizzatoreId);
    void eliminaViaggio(Long id, Long organizzatoreId);
    ViaggioDTO modificaViaggio(Long id,ViaggioDTO viaggiodto, Long organizzatoreId);
    Map<String, Object> getStatisticheRecensioni(Long viaggioId);

    Page<ViaggioDTO> ricercaFiltrata(ViaggioSpecification.ViaggioFilter viaggioFilter, Long UtenteId, int page);
    List<ViaggioMappaDTO> getViaggiMappa();
}