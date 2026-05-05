package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ImmagineViaggioService;
import com.example.progettoenterprise.dto.ImmagineViaggioDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/viaggi/{viaggioId}/immagini")
@RequiredArgsConstructor
@Slf4j
public class ImmagineViaggioController {
    private final ImmagineViaggioService immagineService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> aggiungiImmagine(
            @PathVariable Long viaggioId,
            @RequestParam String url,
            @RequestParam(defaultValue = "true") boolean pubblica,
            @AuthenticationPrincipal UtenteLoggato organizzatore){

        log.info("L'organizzatore {} sta aggiungendo una nuova immagine al viaggio ID: {}",
                organizzatore.getUsername(), viaggioId);

        ImmagineViaggioDTO nuovaImmagine = immagineService.aggiungiImmagine(viaggioId, url, pubblica, organizzatore.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaImmagine);
    }

    @DeleteMapping("/{immagineId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> eliminaImmagine(
            @PathVariable Long viaggioId,
            @PathVariable Long immagineId,
            @AuthenticationPrincipal UtenteLoggato organizzatore){

        log.warn("ELIMINAZIONE: L'organizzatore {} sta eliminando l'immagine ID: {} dal viaggio ID: {}",
                organizzatore.getUsername(), immagineId, viaggioId);

        immagineService.eliminaImmagine(viaggioId, immagineId, organizzatore.getId());
        return ResponseEntity.ok(Map.of("messaggio", "Immagine eliminata con successo"));
    }

    @PatchMapping("/{immagineId}/visibilita")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> modificaVisibilita(
            @PathVariable Long viaggioId,
            @PathVariable Long immagineId,
            @RequestParam boolean pubblica,
            @AuthenticationPrincipal UtenteLoggato organizzatore){

        log.info("L'organizzatore {} sta cambiando la visibilità dell'immagine ID: {} a {}",
                organizzatore.getUsername(), immagineId, pubblica);

        ImmagineViaggioDTO aggiornata = immagineService.modificaVisibilita(viaggioId, immagineId, pubblica, organizzatore.getId());
        return ResponseEntity.ok(aggiornata);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getGalleria(@PathVariable Long viaggioId, @AuthenticationPrincipal UtenteLoggato utente){

        log.info("L'utente {} sta visualizzando la galleria del viaggio ID: {}",
                utente.getUsername(), viaggioId);

        return ResponseEntity.ok(immagineService.getGalleriaViaggio(viaggioId, utente.getId()));
    }

}
