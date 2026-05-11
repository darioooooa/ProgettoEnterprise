package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.AttivitaViaggioSpecification;
import com.example.progettoenterprise.dto.AttivitaViaggioDTO;
import org.springframework.data.domain.Page;


public interface AttivitaViaggioService {
    AttivitaViaggioDTO creaAttivita(Long viaggioId, AttivitaViaggioDTO attivitaViaggioDTO, Long organizzatoreId);
    AttivitaViaggioDTO getAttivitaById(Long id, Long viaggioId, Long utenteId);
    AttivitaViaggioDTO modificaAttivitaViaggio(Long id,AttivitaViaggioDTO dto, Long organizzatoreId);
    void eliminaAttivitaViaggio(Long idAttivita,Long idViaggio, Long organizzatoreId);
    Page<AttivitaViaggioDTO> ricercaFiltrata(AttivitaViaggioSpecification.AttivitaFilter attivitaFilter, Long viaggioId, Long utenteId, int page);
}
