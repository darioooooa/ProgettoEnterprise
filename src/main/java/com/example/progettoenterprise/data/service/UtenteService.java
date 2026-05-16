package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.UtenteSpecification;
import com.example.progettoenterprise.dto.UtenteDTO;
import org.springframework.data.domain.Page;

public interface UtenteService {
    UtenteDTO getProfiloById(Long id);
    UtenteDTO findByUsername(String username);
    UtenteDTO aggiornaProfilo(Long id, UtenteDTO utenteDto);
    void aggiornaPassword(Long id, String vecchiaPassword, String nuovaPassword);
    void eliminaAccount(Long id);
    Page<UtenteDTO> ricercaUtenti(UtenteSpecification.UtenteFilter utenteFilter, int page);
}
