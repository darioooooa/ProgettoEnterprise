package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.AttivitaViaggioSpecification;
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

        AttivitaViaggioDTO nuovaAttivita = attivitaViaggioService.creaAttivita(viaggioId, attivitaViaggioDTO, utenteLoggato.getId());
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

        return ResponseEntity.ok(attivitaViaggioService.getAttivitaById(id, viaggioId, utenteLoggato.getId()));
    }

    @PutMapping("/{attivitaId}/modifica")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<AttivitaViaggioDTO> modificaAttivita(
            @PathVariable Long viaggioId,
            @PathVariable Long attivitaId,
            @Valid @RequestBody AttivitaViaggioDTO dto,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'organizzatore {} sta modificando l'attività numero {} del viaggio {}",
                utenteLoggato.getUsername(), attivitaId, viaggioId);

        return ResponseEntity.ok(attivitaViaggioService.modificaAttivitaViaggio(attivitaId, dto, utenteLoggato.getId()));
    }

    @DeleteMapping("/{attivitaId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> cancellaAttivitaViaggio(
            @PathVariable Long viaggioId,
            @PathVariable Long attivitaId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("Attenzione: l'organizzatore {} sta eliminando l'attività numero {} dal viaggio {}",
                utenteLoggato.getUsername(), attivitaId, viaggioId);

        attivitaViaggioService.eliminaAttivitaViaggio(attivitaId, viaggioId, utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Attività eliminata con successo dal viaggio."));
    }

    // Ricerca avanzata delle attività tramite filtri dinamici
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttivitaViaggioDTO>> ricercaAttivita(
            @PathVariable Long viaggioId,
            AttivitaViaggioSpecification.AttivitaFilter attivitaFilter,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato
    ){
        log.info("L'utente {} sta effettuando una ricerca avanzata sulle attività del viaggio {}, con i filtri {}",
                utenteLoggato.getUsername(), viaggioId, attivitaFilter);
        List<AttivitaViaggioDTO> risultati = attivitaViaggioService.ricercaFiltrata(attivitaFilter, viaggioId, utenteLoggato.getId());
        return ResponseEntity.ok(risultati);
    }
}