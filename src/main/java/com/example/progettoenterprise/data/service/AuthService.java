package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.LoginDTO;
import com.example.progettoenterprise.dto.RegistrazioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;

import java.util.Map;

public interface AuthService {

    UtenteDTO registraUtente(RegistrazioneDTO regDTO);

    Map<String, String> eseguiLogin(LoginDTO loginDTO) throws Exception;

}
