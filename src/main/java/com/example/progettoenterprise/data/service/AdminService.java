package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface AdminService {
    void approvaRichiesta(Long richiestaId, Long adminIdCorrente);
    List<RichiestaPromozioneDTO> getRichieste();
    void rifiutaRichiesta(Long richiestaId, String noteAdmin, Long adminIdCorrente);
    void banUtente(Long userId);
    List<UtenteDTO> getUtentiBannati();
    void sbannaUtente(Long userId);
    Resource scaricaDocumentoCandidatura(Long idRichiesta);
}