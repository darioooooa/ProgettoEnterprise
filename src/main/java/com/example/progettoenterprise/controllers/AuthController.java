package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.AuthService;
import com.example.progettoenterprise.dto.RegistrazioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller che gestisce le operazioni di registrazione
@RestController
@RequestMapping(path="/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // Endpoint per registrare un nuovo utente
    // @Valid valida i dati del corpo della richiesta
    @PostMapping(path = "/register")
    public ResponseEntity<UtenteDTO> registrazione(@Valid @RequestBody RegistrazioneDTO regDTO){
        log.info("Ricevuta richiesta di registrazione per l'utente: {}", regDTO.getUsername());
        UtenteDTO utenteCreato = authService.registraUtente(regDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(utenteCreato);
    }

}
