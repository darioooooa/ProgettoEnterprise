package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.PrenotazioneSpecification;
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
    @PostMapping("/viaggi/{viaggioId}/prenota")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<PrenotazioneDTO> creaPrenotazione(
            @PathVariable Long viaggioId,
            @Valid @RequestBody PrenotazioneDTO richiesta,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta prenotando il viaggio ID: {} per {} persone",
                utenteLoggato.getUsername(), viaggioId, richiesta.getNumeroPersone());

        PrenotazioneDTO nuovaPrenotazione = prenotazioneService.creaPrenotazione(
                viaggioId,
                utenteLoggato.getId(),
                richiesta.getNumeroPersone()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaPrenotazione);
    }

    // RICERCA FILTRATA DELLE PRENOTAZIONI
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PrenotazioneDTO>> getPrenotazioni(
            PrenotazioneSpecification.PrenotazioneFilter prenotazioneFilter,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.info("L'utente {} sta cercando prenotazioni con i filtri: {}", utenteLoggato.getUsername(), prenotazioneFilter);
        List<PrenotazioneDTO> risultati = prenotazioneService.ricercaFiltrata(prenotazioneFilter, utenteLoggato.getId());
        return ResponseEntity.ok(risultati);
    }

    // DETTAGLIO SINGOLA PRENOTAZIONE
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrenotazioneDTO> getDettaglioPrenotazione(@PathVariable Long id, @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Richiesta dettaglio per la prenotazione ID: {}", id);
        return ResponseEntity.ok(prenotazioneService.getPrenotazioneById(id, utenteLoggato.getId()));
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
