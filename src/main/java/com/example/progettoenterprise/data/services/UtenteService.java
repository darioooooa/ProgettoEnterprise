package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.dto.UtenteDTO;
import java.util.List;
public interface UtenteService {
    UtenteDTO getProfiloById(Long id);

    UtenteDTO aggiornaProfilo(Long id, UtenteDTO utenteDto);
    List<UtenteDTO> cercaUtenti(String query);
    void eliminaAccount(Long id);
}
