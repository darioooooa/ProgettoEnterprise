package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.SegnalazioneSpecification;
import com.example.progettoenterprise.data.service.SegnalazioneService;
import com.example.progettoenterprise.dto.SegnalazioneDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/segnalazioni")
@RequiredArgsConstructor
public class SegnalazioneController {

    private final SegnalazioneService segnalazioneService;
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/crea")
    public ResponseEntity<SegnalazioneDTO> creaSegnalazione(
            @RequestBody SegnalazioneDTO segnalazioneDTO,
            @RequestParam Long idSegnalatore) {

        log.info("Ricevuta richiesta di creazione segnalazione da parte dell'utente ID: {}", idSegnalatore);
        SegnalazioneDTO creata = segnalazioneService.creaSegnalazione(segnalazioneDTO, idSegnalatore);
        return ResponseEntity.status(HttpStatus.CREATED).body(creata);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ricerca")
    public ResponseEntity<List<SegnalazioneDTO>> cercaSegnalazioni(
            @ModelAttribute SegnalazioneSpecification.SegnalazioneFilter filtro,
            @RequestParam(defaultValue = "0") int pagina) {

        log.info("Ricerca segnalazioni in corso. Pagina richiesta: {}", pagina);
        List<SegnalazioneDTO> risultati = segnalazioneService.cercaSegnalazioni(filtro, pagina);
        return ResponseEntity.ok(risultati);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/prendi-in-carico")
    public ResponseEntity<SegnalazioneDTO> prendiInCarico(
            @PathVariable Long id,
            @RequestParam Long idAdmin) {

        log.info("L'amministratore ID: {} sta provando a prendere in carico la segnalazione ID: {}", idAdmin, id);
        SegnalazioneDTO aggiornata = segnalazioneService.prendiInCarico(id, idAdmin);
        return ResponseEntity.ok(aggiornata);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/risolvi")
    public ResponseEntity<SegnalazioneDTO> risolviSegnalazione(
            @PathVariable Long id,
            @RequestParam Long idAdmin) {

        log.info("L'amministratore ID: {} sta risolvendo la segnalazione ID: {}", idAdmin, id);
        SegnalazioneDTO risolta = segnalazioneService.risolviSegnalazione(id, idAdmin);
        return ResponseEntity.ok(risolta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/rifiuta")
    public ResponseEntity<SegnalazioneDTO> rifiutaSegnalazione(
            @PathVariable Long id,
            @RequestParam Long idAdmin) {

        log.info("L'amministratore ID: {} sta rifiutando la segnalazione ID: {}", idAdmin, id);
        SegnalazioneDTO rifiutata = segnalazioneService.rifiutaSegnalazione(id, idAdmin);
        return ResponseEntity.ok(rifiutata);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/contatore-aperte")
    public ResponseEntity<Long> contaSegnalazioniAperte() {
        log.info("Richiesto il conteggio totale delle segnalazioni aperte");
        long conteggio = segnalazioneService.contaSegnalazioniAperte();
        return ResponseEntity.ok(conteggio);
    }
}