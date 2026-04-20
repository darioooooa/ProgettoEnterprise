package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.dto.LoginDTO;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.security.TokenStore;
import com.example.progettoenterprise.data.services.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Controller che gestisce le operazioni di autenticazione e registrazione
@RestController
@RequestMapping(path="/api/v1/auth", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;

    // Endpoint per registrare un nuovo utente
    // @Valid valida i dati del corpo della richiesta
    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<?> registrazione(@Valid @RequestBody Utente utente){
        return ResponseEntity.ok(authServiceImpl.registraUtente(utente));
    }

    // Endpoint per il login dell'utente
    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> login (@RequestBody LoginDTO loginDTO) throws Exception{
        // Valida le credenziali
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        // Cerca l'utente
        Utente utente = authServiceImpl.getUtenteByUsernameOrEmail(loginDTO.getUsername());

        // Crea il token, includendo le informazioni dell'utente nel payload
        String token = tokenStore.createToken(Map.of(
                "username", utente.getUsername(),
                "role", "ROLE_" +utente.getRuolo().name()
        ));

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(Map.of(
                        "token", token,
                        "email", utente.getEmail(),
                        "username", utente.getUsername(),
                        "ruolo", utente.getRuolo().name()
                ));
    }

    // Endpoint per il logout dell'utente
    @PostMapping("/logout")
    public ResponseEntity<?> logout(){

        // Cancella il contesto di autenticazione
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of(
                "messaggio", "Logout effettuato con successo"));
        // Il token continua a funzionare, andrà poi eliminato lato client,
        // questo per mantenere una struttura totalmemte stateless
    }
}
