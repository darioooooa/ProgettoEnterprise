package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ViaggioDTO;
import java.util.List;

public interface RaccomandazioneService {
    List<ViaggioDTO> getConsigliatiPerUtente(Long utenteId);
}
