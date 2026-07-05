package com.example.progettoenterprise.data.service;
import com.example.progettoenterprise.data.repositories.specifications.SegnalazioneSpecification;
import com.example.progettoenterprise.dto.SegnalazioneDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SegnalazioneService {
    SegnalazioneDTO creaSegnalazione(SegnalazioneDTO segnalazioneDTO, Long idSegnalatore);
    org.springframework.data.domain.Page<SegnalazioneDTO> cercaSegnalazioni(SegnalazioneSpecification.SegnalazioneFilter filtro, int pagina, int dimensione);
    SegnalazioneDTO prendiInCarico(Long segnalazioneId, Long adminId);
    SegnalazioneDTO risolviSegnalazione(Long idSegnalazione, Long idAdmin, boolean sospendiAutore);

    SegnalazioneDTO rifiutaSegnalazione(Long segnalazioneId, Long adminId);
    long contaSegnalazioniAperte();
}
