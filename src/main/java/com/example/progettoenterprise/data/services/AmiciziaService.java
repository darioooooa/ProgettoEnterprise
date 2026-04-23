package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.dto.AmiciziaDTO;
import java.util.List;

public interface AmiciziaService {
    AmiciziaDTO inviaRichiesta(Long richiedenteId, Long riceventeId);
    AmiciziaDTO accettaRichiesta(Long amiciziaId, Long riceventeId);
    List<AmiciziaDTO> getMieiAmici(Long utenteId);
    List<AmiciziaDTO> getRichiesteRicevute(Long utenteId);
}