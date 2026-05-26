package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminService {
    void approvaRichiesta(Long richiestaId, Long adminIdCorrente);
    List<RichiestaPromozioneDTO> getRichieste();
    void rifiutaRichiesta(Long richiestaId, String noteAdmin,Long adminId);
    void banUtente(Long id);
}
