package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.dto.UtenteDTO;

public interface AuthService {

    UtenteDTO registraUtente(Utente utente);

    Utente getUtenteByUsernameOrEmail(String username);

}
