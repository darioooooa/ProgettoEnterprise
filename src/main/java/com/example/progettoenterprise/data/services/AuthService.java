package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final UtenteRepository utenteRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Metodo per registrare un nuovo utente
    public Utente registraUtente(Utente utente){

        // Controlla se l'email esiste già
        if (utenteRepository.findByEmail(utente.getEmail()).isPresent()) {
            throw new RuntimeException("Errore: l'email è già in uso");
        }

        // Controlla se lo username esiste già
        if (utenteRepository.findByUsername(utente.getUsername()).isPresent()) {
            throw new RuntimeException("Errore: lo username è già in uso");
        }

        // Cifratura della password
        utente.setPassword(passwordEncoder.encode(utente.getPassword()));

        // Impostazione del ruolo predefinito
        utente.setRuolo(Utente.Ruolo.VIAGGIATORE);

        return utenteRepository.save(utente);
    }

    // Metodo per ottenere un utente dallo username
    public Utente getUtenteByUsername(String username){
        return utenteRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con username: " + username));
    }
}
