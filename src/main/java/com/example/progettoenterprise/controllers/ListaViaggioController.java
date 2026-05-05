package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ListaViaggioService;
import com.example.progettoenterprise.dto.ListaViaggioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/api/v1/lista-viaggi", produces = "application/json")
@RequiredArgsConstructor
public class ListaViaggioController {
    private final ListaViaggioService listaViaggioService;
    @GetMapping("/{viaggioId}/itinerari")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ListaViaggioDTO>> getProgramma(@PathVariable Long viaggioId){
        List<ListaViaggioDTO> programma = listaViaggioService.getProgrammaCompleto(viaggioId);
        return ResponseEntity.ok(programma);


    }

    @PostMapping("/{viaggioId}/itinerari/{itinerarioId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<ListaViaggioDTO> aggiungiItinerario(@PathVariable Long viaggioId, @PathVariable Long itinerarioId){
        ListaViaggioDTO itinerarioAggiunto = listaViaggioService.aggiungiItinerarioAlViaggio(viaggioId, itinerarioId);
        return ResponseEntity.ok(itinerarioAggiunto);
    }
    @DeleteMapping("/{viaggioId}/itinerari/{itinerarioId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> rimuoviItinerario(@PathVariable Long viaggioId, @PathVariable Long itinerarioId){
        listaViaggioService.rimuoviItinerarioDalViaggio(viaggioId, itinerarioId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/ricerca-per-budget")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ListaViaggioDTO>>cercaPerBudget(@RequestParam Double budget){
        List<ListaViaggioDTO> itinerari = listaViaggioService.cercaItinerariSottoBudget(budget);
        return ResponseEntity.ok(itinerari);
    }

}
