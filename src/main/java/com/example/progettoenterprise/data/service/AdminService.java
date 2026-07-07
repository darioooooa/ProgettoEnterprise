package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.RichiestaPromozioneSpecification;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminService {
    void approvaRichiesta(Long richiestaId, Long adminIdCorrente);
    List<RichiestaPromozioneDTO> getRichieste();

    Page<RichiestaPromozioneDTO> getRichiestePaginate(RichiestaPromozioneSpecification.RichiestaFilter filter, int page, int size);

    void rifiutaRichiesta(Long richiestaId, String noteAdmin, Long adminIdCorrente);
    void banUtente(Long userId);
    List<UtenteDTO> getUtentiBannati();
    void sbannaUtente(Long userId);
    Resource scaricaDocumentoCandidatura(Long idRichiesta);
    Page<UtenteDTO> getUtentiBannatiPaginati(int page, int size, String ricerca);
}