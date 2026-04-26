package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.RecensioneService;
import com.example.progettoenterprise.dto.RecensioneDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="/api/v1/viaggi/{viaggioId}/recensioni", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class RecensioneController {
    private final RecensioneService recensioneService;

    // Endpoint per creare una nuova recensione
    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<?> creaRecensione(
            @PathVariable Long viaggioId,
            @Valid @RequestBody RecensioneDTO recDTO,
            @AuthenticationPrincipal UserDetails userDetails){

        // Recupera lo username dell'utente autenticato
        String username = userDetails.getUsername();
        recensioneService.aggiungiRecensione(username, viaggioId, recDTO.getVoto(), recDTO.getCommento());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Recensione aggiunta con successo!"));
    }

    // Endpoint per ottenere tutte le recensioni di un viaggio
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecensioniViaggio(@PathVariable Long viaggioId){
        List<RecensioneDTO> recensioni = recensioneService.getRecensioniViaggio(viaggioId);
        return ResponseEntity.ok(recensioni);
    }

    // Endpoint per aggiornare una recensione
    @PutMapping(value = "/{recensioneId}", consumes = "application/json")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<?> aggiornaRecensione(
            @PathVariable Long viaggioId,
            @PathVariable Long recensioneId,
            @Valid @RequestBody RecensioneDTO recDTO,
            @AuthenticationPrincipal UserDetails userDetails){

        String username = userDetails.getUsername();
        recensioneService.aggiornaRecensione(viaggioId, recensioneId, username, recDTO.getVoto(), recDTO.getCommento());
        return ResponseEntity.ok(Map.of("message", "Recensione aggiornata con successo!"));
    }

    // Endpoint per eliminare una recensione
    @DeleteMapping(value = "/{recensioneId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminaRecensione(
            @PathVariable Long viaggioId,
            @PathVariable Long recensioneId,
            @AuthenticationPrincipal UserDetails userDetails){

        String username = userDetails.getUsername();
        recensioneService.eliminaRecensione(viaggioId, recensioneId, username);
        return ResponseEntity.ok(Map.of("message", "Recensione eliminata con successo!"));
    }
}
