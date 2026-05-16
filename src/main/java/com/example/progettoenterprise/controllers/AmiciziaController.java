package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.AmiciziaService;
import com.example.progettoenterprise.dto.AmiciziaDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="/api/v1/amicizie", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class AmiciziaController {

    private final AmiciziaService amiciziaService;

    // ENDPOINT PER INVIO RICHIESTA DI AMICIZIA
    @PostMapping("/richiesta/{riceventeUsername}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AmiciziaDTO> inviaRichiesta(
            @PathVariable String riceventeUsername,
            @AuthenticationPrincipal UtenteLoggato mittente) {

        log.info("L'utente {} (ID: {}) sta inviando una richiesta di amicizia a: {}",
                mittente.getUsername(), mittente.getId(), riceventeUsername);

        AmiciziaDTO nuovaAmicizia = amiciziaService.inviaRichiesta(mittente.getId(), riceventeUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaAmicizia);
    }

    // ENDPOINT PER ACCETTAZIONE RICHIESTA DI AMICIZIA
    @PatchMapping("/{amiciziaId}/accetta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AmiciziaDTO> accettaRichiesta(
            @PathVariable Long amiciziaId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta accettando la richiesta di amicizia ID: {}",
                utenteLoggato.getUsername(), amiciziaId);

        AmiciziaDTO amiciziaAccettata = amiciziaService.accettaRichiesta(amiciziaId, utenteLoggato.getId());
        return ResponseEntity.ok(amiciziaAccettata);
    }

    // ENDPOINT PER OTTENERE LA LISTA AMICI
    @GetMapping("/miei-amici")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AmiciziaDTO>> getMieiAmici(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero lista amici per l'utente: {}", utenteLoggato.getUsername());
        List<AmiciziaDTO> amici = amiciziaService.getMieiAmici(utenteLoggato.getId());
        return ResponseEntity.ok(amici);
    }

    // ENDPOINT PER OTTENERE LA LISTA RICHIESTE RICEVUTE
    @GetMapping("/richieste/ricevute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AmiciziaDTO>> getRichiesteRicevute(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero richieste ricevute per: {}", utenteLoggato.getUsername());
        return ResponseEntity.ok(amiciziaService.getRichiesteRicevute(utenteLoggato.getId()));
    }

    // ENDPOINT PER OTTENERE LA LISTA RICHIESTE INVIATE
    @GetMapping("/richieste/inviate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AmiciziaDTO>> getRichiesteInviate(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero richieste inviate da: {}", utenteLoggato.getUsername());
        return ResponseEntity.ok(amiciziaService.getRichiesteInviate(utenteLoggato.getId()));
    }

    // RIFIUTA UNA RICHIESTA
    @PatchMapping("/{amiciziaId}/rifiuta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rifiutaRichiesta(
            @PathVariable Long amiciziaId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("L'utente {} sta rifiutando la richiesta di amicizia ID: {}",
                utenteLoggato.getUsername(), amiciziaId);

        amiciziaService.rifiutaRichiesta(amiciziaId, utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Richiesta di amicizia rifiutata con successo"));
    }

    // RIMUOVERE UN AMICO
    @DeleteMapping("/rimuovi/{amicoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rimuoviAmico(
            @PathVariable Long amicoId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("L'utente {} sta rimuovendo l'amico ID: {}",
                utenteLoggato.getUsername(), amicoId);

        amiciziaService.rimuoviAmico(utenteLoggato.getId(), amicoId);
        return ResponseEntity.ok(Map.of("message", "Amicizia rimossa con successo"));
    }
}