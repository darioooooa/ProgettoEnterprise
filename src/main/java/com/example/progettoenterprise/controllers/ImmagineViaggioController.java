package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ImmagineViaggioService;
import com.example.progettoenterprise.dto.ImmagineViaggioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/viaggi/{viaggioId}/immagini")
@RequiredArgsConstructor
public class ImmagineViaggioController {
    private final ImmagineViaggioService immagineService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> aggiungiImmagine(
            @PathVariable Long viaggioId,
            @RequestParam String url,
            @RequestParam(defaultValue = "true") boolean pubblica,
            @AuthenticationPrincipal UserDetails userDetails){

        ImmagineViaggioDTO nuovaImmagine = immagineService.aggiungiImmagine(viaggioId, url, pubblica, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaImmagine);
    }

    @DeleteMapping("/{immagineId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> eliminaImmagine(
            @PathVariable Long viaggioId,
            @PathVariable Long immagineId,
            @AuthenticationPrincipal UserDetails userDetails){
        immagineService.eliminaImmagine(viaggioId, immagineId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("messaggio", "Immagine eliminata con successo"));
    }

    @PatchMapping("/{immagineId}/visibilita")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> modificaVisibilita(
            @PathVariable Long viaggioId,
            @PathVariable Long immagineId,
            @RequestParam boolean pubblica,
            @AuthenticationPrincipal UserDetails userDetails){
        ImmagineViaggioDTO aggiornata = immagineService.modificaVisibilita(viaggioId, immagineId, pubblica, userDetails.getUsername());
        return ResponseEntity.ok(aggiornata);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getGalleria(@PathVariable Long viaggioId, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(immagineService.getGalleriaViaggio(viaggioId, userDetails.getUsername()));
    }

}
