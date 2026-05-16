package com.example.progettoenterprise.data.service;
import com.example.progettoenterprise.data.repositories.specifications.SegnalazioneSpecification;
import com.example.progettoenterprise.dto.SegnalazioneDTO;

import java.util.List;

public interface SegnalazioneService {
    SegnalazioneDTO creaSegnalazione(SegnalazioneDTO segnalazioneDTO, Long idSegnalatore);
    List<SegnalazioneDTO> cercaSegnalazioni(SegnalazioneSpecification.SegnalazioneFilter filtro, int pagina);
    SegnalazioneDTO prendiInCarico(Long segnalazioneId, Long adminId);
    SegnalazioneDTO risolviSegnalazione(Long segnalazioneId, Long adminId);
    SegnalazioneDTO rifiutaSegnalazione(Long segnalazioneId, Long adminId);
    long contaSegnalazioniAperte();
}
