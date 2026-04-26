package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.UtenteDTO;
import java.util.List;
public interface UtenteService {
    UtenteDTO getProfiloById(Long id);

    UtenteDTO aggiornaProfilo(Long id, UtenteDTO utenteDto);
    void aggiornaPassword(Long id, String vecchiaPassword, String nuovaPassword);
    List<UtenteDTO> cercaUtenti(String query);
    void eliminaAccount(Long id);
}
