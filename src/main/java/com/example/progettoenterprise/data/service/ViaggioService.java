package com.example.progettoenterprise.data.service;


import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.dto.ViaggioDTO;
import com.example.progettoenterprise.dto.ViaggioMappaDTO;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ViaggioService {
    ViaggioDTO creaViaggio(ViaggioDTO viaggiodto, Long organizzatoreId);
    void eliminaViaggio(Long id, Long organizzatoreId);
    ViaggioDTO modificaViaggio(Long id,ViaggioDTO viaggiodto, Long organizzatoreId);
    Map<String, Object> getStatisticheRecensioni(Long viaggioId);

    Page<ViaggioDTO> ricercaFiltrata(ViaggioSpecification.ViaggioFilter viaggioFilter, Long UtenteId, int page);
    List<ViaggioMappaDTO> getViaggiMappa(Long organizzatoreId);

    @Nullable ViaggioDTO getViaggioById(Long viaggioId, Long id);
    ViaggioDTO getViaggioById(Long id);
    List<ViaggioDTO> getViaggiByOrganizzatore(Long organizzatoreId);
    // Trova i viaggi che iniziano tra 'oggi' e 'limite',ad esempio tra 3 giorni e che sono ancora aperti
    List<Viaggio> getViaggiInPartenza(LocalDate oggi, LocalDate limite);
    List<ViaggioDTO> getConsigliatiPerUtente(Long utenteId);
}