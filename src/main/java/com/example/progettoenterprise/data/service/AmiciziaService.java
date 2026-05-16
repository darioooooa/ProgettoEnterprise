package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.AmiciziaDTO;
import java.util.List;

public interface AmiciziaService {
    AmiciziaDTO inviaRichiesta(Long richiedenteId, String riceventeUsername);
    AmiciziaDTO accettaRichiesta(Long amiciziaId, Long riceventeId);
    List<AmiciziaDTO> getMieiAmici(Long utenteId);
    List<AmiciziaDTO> getRichiesteRicevute(Long utenteId);
    void rifiutaRichiesta(Long amiciziaId, Long riceventeId);
    void rimuoviAmico(Long richiedenteId, Long riceventeId);
    List<AmiciziaDTO> getRichiesteInviate(Long utenteId);
}