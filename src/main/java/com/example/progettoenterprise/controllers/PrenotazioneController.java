package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.PrenotazioneService;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prenotazioni")
@RequiredArgsConstructor
@Slf4j
public class PrenotazioneController {

    private final PrenotazioneService prenotazioneService;

    // CREA UNA NUOVA PRENOTAZIONE
    @PostMapping
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<PrenotazioneDTO> creaPrenotazione(
            @Valid @RequestBody PrenotazioneDTO richiesta,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta prenotando il viaggio ID: {} per {} persone",
                utenteLoggato.getUsername(), richiesta.getViaggioId(), richiesta.getNumeroPersone());

        PrenotazioneDTO nuovaPrenotazione = prenotazioneService.creaPrenotazione(
                richiesta.getViaggioId(),
                utenteLoggato.getId(),
                richiesta.getNumeroPersone()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaPrenotazione);
    }

    // VISUALIZZA LE PROPRIE PRENOTAZIONI
    @GetMapping("/mie-prenotazioni")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<List<PrenotazioneDTO>> getMiePrenotazioni(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero elenco prenotazioni per l'utente: {}", utenteLoggato.getUsername());
        List<PrenotazioneDTO> lista = prenotazioneService.getPrenotazioneperUtente(utenteLoggato.getId());
        return ResponseEntity.ok(lista);
    }

    // DETTAGLIO SINGOLA PRENOTAZIONE
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrenotazioneDTO> getDettaglioPrenotazione(@PathVariable Long id) {

        log.info("Richiesta dettaglio per la prenotazione ID: {}", id);
        return ResponseEntity.ok(prenotazioneService.getPrenotazioneById(id));
    }

    // CANCELLA PRENOTAZIONE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<?> cancellaPrenotazione(
            @PathVariable Long id,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("L'utente {} sta cancellando la prenotazione ID: {}", utenteLoggato.getUsername(), id);
        prenotazioneService.cancellaPrenotazione(id, utenteLoggato.getId());

        return ResponseEntity.ok(Map.of("message", "Prenotazione cancellata con successo"));
    }

    // ESPORTA CALENDARIO ICS
    @GetMapping("/{prenotazioneId}/esporta-calendario")
    public ResponseEntity<byte[]> scaricaFileCalendario(@PathVariable Long prenotazioneId) {

        byte[] datiCalendario = prenotazioneService.esportaPrenotazioni(prenotazioneId);

        HttpHeaders intestazioni = new HttpHeaders();
        String nomeFile = "prenotazione_" + prenotazioneId + ".ics";
        intestazioni.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeFile);
        intestazioni.add(HttpHeaders.CONTENT_TYPE, "text/calendar");

        return new ResponseEntity<>(datiCalendario, intestazioni, HttpStatus.OK);
    }
}
