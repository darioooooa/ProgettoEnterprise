package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.AttivitaViaggioService;
import com.example.progettoenterprise.dto.AttivitaViaggioDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/viaggi/{viaggioId}/attivita-viaggi")
@RequiredArgsConstructor
@Slf4j
public class AttivitaViaggioController {

    private final AttivitaViaggioService attivitaViaggioService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<AttivitaViaggioDTO> creaAttivitaViaggio(
            @PathVariable Long viaggioId,
            @Valid @RequestBody AttivitaViaggioDTO attivitaViaggioDTO,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'organizzatore {} sta creando una nuova attività per il viaggio numero {}",
                utenteLoggato.getUsername(), viaggioId);

        AttivitaViaggioDTO nuovaAttivita = attivitaViaggioService.creaAttivita(attivitaViaggioDTO);
        return new ResponseEntity<>(nuovaAttivita, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttivitaViaggioDTO> getAttivitaById(
            @PathVariable Long viaggioId,
            @PathVariable Long id,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta consultando il dettaglio dell'attività numero {} (viaggio {})",
                utenteLoggato.getUsername(), id, viaggioId);

        return ResponseEntity.ok(attivitaViaggioService.getAttivitaById(id));
    }

    @GetMapping("/timeline")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttivitaViaggioDTO>> getTimelineSpostamenti(
            @PathVariable Long viaggioId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta richiedendo la linea temporale delle attività per il viaggio {}",
                utenteLoggato.getUsername(), viaggioId);

        return ResponseEntity.ok(attivitaViaggioService.getTimelineSpostamenti(viaggioId));
    }

    @GetMapping("/cerca")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttivitaViaggioDTO>> cercaInViaggio(
            @PathVariable Long viaggioId,
            @RequestParam String keyword,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta cercando la parola '{}' nelle attività del viaggio {}",
                utenteLoggato.getUsername(), keyword, viaggioId);

        return ResponseEntity.ok(attivitaViaggioService.cercaInViaggio(viaggioId, keyword));
    }

    @PutMapping("/{id}/modifica")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<AttivitaViaggioDTO> modificaAttivita(
            @PathVariable Long viaggioId,
            @PathVariable Long id,
            @Valid @RequestBody AttivitaViaggioDTO dto,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'organizzatore {} sta modificando l'attività numero {} del viaggio {}",
                utenteLoggato.getUsername(), id, viaggioId);

        return ResponseEntity.ok(attivitaViaggioService.modificaAttivitaViaggio(id, dto));
    }

    @GetMapping("/filtra-per-budget")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttivitaViaggioDTO>> filtraPerBudget(
            @PathVariable Long viaggioId,
            @RequestParam Double budgetMax,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta filtrando le attività del viaggio {} con un limite di spesa di {} euro",
                utenteLoggato.getUsername(), viaggioId, budgetMax);

        return ResponseEntity.ok(attivitaViaggioService.filtraPerBudget(viaggioId, budgetMax));
    }

    @DeleteMapping("/{attivitaId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> cancellaAttivitaViaggio(
            @PathVariable Long viaggioId,
            @PathVariable Long attivitaId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("Attenzione: l'organizzatore {} sta eliminando l'attività numero {} dal viaggio {}",
                utenteLoggato.getUsername(), attivitaId, viaggioId);

        attivitaViaggioService.eliminaAttivitaViaggio(attivitaId, viaggioId);
        return ResponseEntity.ok(Map.of("message", "Attività eliminata con successo dal viaggio."));
    }
}