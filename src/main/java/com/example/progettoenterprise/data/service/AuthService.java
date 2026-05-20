package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.RegistrazioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;

public interface AuthService {

    UtenteDTO registraUtente(RegistrazioneDTO dto);
}
