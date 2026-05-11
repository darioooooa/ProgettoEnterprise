package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.UtenteSpecification;
import com.example.progettoenterprise.data.service.UtenteService;
import com.example.progettoenterprise.dto.UtenteDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/utenti")
@RequiredArgsConstructor
@Slf4j
public class UtenteController {

    private final UtenteService utenteService;

    // VISUALIZZARE IL PROPRIO PROFILO: UTILIZO COME ENDPOINT ME E NON L'ID DELL'UTENTE
    // PER UNA QUESTIONE DI SICUREZZA
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UtenteDTO> getMioProfilo(@AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta visualizzando il proprio profilo", utenteLoggato.getUsername());
        UtenteDTO profilo = utenteService.getProfiloById(utenteLoggato.getId());
        return ResponseEntity.ok(profilo);
    }

    // RICERCA DEGLI UTENTI
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UtenteDTO>> getUtenti(UtenteSpecification.UtenteFilter utenteFilter, @RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'Admin {} sta cercando utenti (pag: {}) con i filtri: {}", utenteLoggato.getUsername(), page, utenteFilter);
        Page<UtenteDTO> risultati = utenteService.ricercaUtenti(utenteFilter, page);
        return ResponseEntity.ok(risultati);
    }

    // VISUALIZZA IL PROFILO DI UN ALTRO UTENTE
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UtenteDTO> getProfiloById(@PathVariable Long id) {

        log.info("Richiesta visualizzazione profilo per l'utente ID: {}", id);
        UtenteDTO profilo = utenteService.getProfiloById(id);
        return ResponseEntity.ok(profilo);
    }

    // AGGIORNA IL PROPRIO PROFILO
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UtenteDTO> aggiornaProfilo(
            @RequestBody UtenteDTO utenteDto,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta aggiornando il proprio profilo", utenteLoggato.getUsername());
        UtenteDTO profiloAggiornato = utenteService.aggiornaProfilo(utenteLoggato.getId(), utenteDto);
        return ResponseEntity.ok(profiloAggiornato);
    }

    // CAMBIO PASSWORD
    @Data
    public static class CambioPasswordRequest {
        private String vecchiaPassword;
        private String nuovaPassword;
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> aggiornaPassword(
            @RequestBody CambioPasswordRequest request,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} ha richiesto il cambio della password", utenteLoggato.getUsername());
        utenteService.aggiornaPassword(
                utenteLoggato.getId(),
                request.getVecchiaPassword(),
                request.getNuovaPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Password aggiornata con successo"));
    }

    // ELIMINA IL PROPRIO ACCOUNT
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminaAccount(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("L'utente {} sta eliminando il proprio account (ID: {})",
                utenteLoggato.getUsername(), utenteLoggato.getId());

        utenteService.eliminaAccount(utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Account eliminato definitivamente"));
    }
}