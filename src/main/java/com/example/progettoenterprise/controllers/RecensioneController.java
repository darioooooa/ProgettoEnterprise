package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.RecensioneSpecification;
import com.example.progettoenterprise.data.service.RecensioneService;
import com.example.progettoenterprise.dto.RecensioneDTO;
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
@RequestMapping(path="/api/v1/viaggi/{viaggioId}/recensioni", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class RecensioneController {
    private final RecensioneService recensioneService;

    // Endpoint per creare una nuova recensione
    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<RecensioneDTO> creaRecensione(
            @PathVariable Long viaggioId,
            @Valid @RequestBody RecensioneDTO recDTO,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato){

        log.info("L'utente {} (ID: {}) sta inserendo una recensione per il viaggio ID: {}",
                utenteLoggato.getUsername(), utenteLoggato.getId(), viaggioId);
        // Recupera lo username dell'utente autenticato
        RecensioneDTO nuovaRecensione = recensioneService.aggiungiRecensione(utenteLoggato.getId(), viaggioId, recDTO.getVoto(), recDTO.getCommento());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaRecensione);
    }

    // Endpoint per ricercare le recensioni di un viaggio
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RecensioneDTO>> getRecensioni(
            @PathVariable Long viaggioId,
            RecensioneSpecification.RecensioneFilter recensioneFilter,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato
    ){
        log.info("L'utente {} sta cercando recensioni per il viaggio ID: {} con i filtri{}",utenteLoggato.getUsername(), viaggioId, recensioneFilter);
        List<RecensioneDTO> risultati = recensioneService.ricercaFiltrata(recensioneFilter, utenteLoggato.getId(), viaggioId);
        return ResponseEntity.ok(risultati);
    }

    // Endpoint per aggiornare una recensione
    @PutMapping(value = "/{recensioneId}", consumes = "application/json")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<RecensioneDTO> aggiornaRecensione(
            @PathVariable Long viaggioId,
            @PathVariable Long recensioneId,
            @Valid @RequestBody RecensioneDTO recDTO,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato){

        log.info("L'utente {} sta tentando di modificare la recensione ID: {} del viaggio ID: {}",
                utenteLoggato.getUsername(), recensioneId, viaggioId);

        RecensioneDTO aggiornata = recensioneService.aggiornaRecensione(viaggioId, recensioneId, utenteLoggato.getId(), recDTO.getVoto(), recDTO.getCommento());
        return ResponseEntity.ok(aggiornata);
    }

    // Endpoint per eliminare una recensione
    @DeleteMapping(value = "/{recensioneId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String,String>> eliminaRecensione(
            @PathVariable Long viaggioId,
            @PathVariable Long recensioneId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato){

        log.warn("ELIMINAZIONE: L'utente {} sta eliminando la recensione ID: {}",
                utenteLoggato.getUsername(), recensioneId);

        recensioneService.eliminaRecensione(viaggioId, recensioneId, utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Recensione eliminata con successo!"));
    }
}
