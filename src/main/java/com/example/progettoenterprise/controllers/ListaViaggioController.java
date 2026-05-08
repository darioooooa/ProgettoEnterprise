package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ListaViaggioService;
import com.example.progettoenterprise.dto.ListaViaggioDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="/api/v1/lista-viaggi", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class ListaViaggioController {

    private final ListaViaggioService listaViaggioService;

    @GetMapping("/{viaggioId}/itinerari")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ListaViaggioDTO>> getProgramma(
            @PathVariable Long viaggioId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta consultando il programma del viaggio identificato dal numero: {}",
                utenteLoggato.getUsername(), viaggioId);

        List<ListaViaggioDTO> programma = listaViaggioService.getProgrammaCompleto(viaggioId);
        return ResponseEntity.ok(programma);
    }

    @PostMapping("/{viaggioId}/itinerari/{itinerarioId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<ListaViaggioDTO> aggiungiItinerario(
            @PathVariable Long viaggioId,
            @PathVariable Long itinerarioId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'organizzatore {} sta aggiungendo l'itinerario numero {} al viaggio numero {}",
                utenteLoggato.getUsername(), itinerarioId, viaggioId);

        ListaViaggioDTO itinerarioAggiunto = listaViaggioService.aggiungiItinerarioAlViaggio(viaggioId, itinerarioId);
        return ResponseEntity.ok(itinerarioAggiunto);
    }

    @DeleteMapping("/{viaggioId}/itinerari/{itinerarioId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> rimuoviItinerario(
            @PathVariable Long viaggioId,
            @PathVariable Long itinerarioId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("Attenzione: l'organizzatore {} sta rimuovendo l'itinerario numero {} dal viaggio numero {}",
                utenteLoggato.getUsername(), itinerarioId, viaggioId);

        listaViaggioService.rimuoviItinerarioDalViaggio(viaggioId, itinerarioId);
        return ResponseEntity.ok(Map.of("message", "Itinerario rimosso con successo dal viaggio."));
    }

    @GetMapping("/ricerca-per-budget")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ListaViaggioDTO>> cercaPerBudget(
            @RequestParam Double budget,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta cercando itinerari con un limite di spesa di: {} euro",
                utenteLoggato.getUsername(), budget);

        List<ListaViaggioDTO> itinerari = listaViaggioService.cercaItinerariSottoBudget(budget);
        return ResponseEntity.ok(itinerari);
    }
}