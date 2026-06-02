package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminService {

    void approvaRichiesta(Long richiestaId, Long adminIdCorrente);

    List<RichiestaPromozioneDTO> getRichieste();

    void rifiutaRichiesta(Long richiestaId, String noteAdmin, Long adminId);

    void banUtente(Long id);

    List<UtenteDTO> getUtentiBannati();

    void sbannaUtente(Long userId);
}