package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.dto.LoginDTO;
import com.example.progettoenterprise.data.service.AuthService;
import com.example.progettoenterprise.dto.RegistrazioneDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Controller che gestisce le operazioni di autenticazione e registrazione
@RestController
@RequestMapping(path="/api/v1/auth", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Endpoint per registrare un nuovo utente
    // @Valid valida i dati del corpo della richiesta
    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<?> registrazione(@Valid @RequestBody RegistrazioneDTO regDTO){
        return ResponseEntity.ok(authService.registraUtente(regDTO));
    }

    // Endpoint per il login dell'utente
    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> login (@Valid @RequestBody LoginDTO loginDTO) throws Exception{

        Map<String, String> datiLogin = authService.eseguiLogin(loginDTO);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + datiLogin.get("token"))
                .body(datiLogin);
    }

    // Endpoint per il logout dell'utente
    @PostMapping("/logout")
    public ResponseEntity<?> logout(){

        // Cancella il contesto di autenticazione (pulisce la sessione corrente nel server)
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("messaggio", "Logout effettuato con successo"));
        // Il token continua a funzionare, andrà poi eliminato lato client,
        // questo per mantenere una struttura totalmemte stateless
    }
}
