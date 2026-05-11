package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/viaggi", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class ViaggioController {
    private final ViaggioService viaggioService;

    // Endpoint per la ricerca filtrata dinamica
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ViaggioDTO>> getViaggi(ViaggioSpecification.ViaggioFilter viaggioFilter, @RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta cercando viaggi (pag: {}) con i filtri: {}", utenteLoggato.getUsername(), page, viaggioFilter);
        Page<ViaggioDTO> viaggiFiltrati = viaggioService.ricercaFiltrata(viaggioFilter, utenteLoggato.getId(), page);
        return ResponseEntity.ok(viaggiFiltrati);
    }

    // Endpoint per creare un nuovo viaggio (organizzatore)
    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<ViaggioDTO> creaViaggio(
            @Valid @RequestBody ViaggioDTO viaggioDTO,
            @AuthenticationPrincipal UtenteLoggato organizzatore) {

        log.info("L'organizzatore {} (ID: {}) sta creando un nuovo viaggio",
                organizzatore.getUsername(), organizzatore.getId());

        ViaggioDTO viaggioCreato = viaggioService.creaViaggio(viaggioDTO, organizzatore.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(viaggioCreato);
    }

    // Endpoint per ottenere le statistiche delle recensioni di un viaggio
    @GetMapping(value = "/{viaggioId}/statistiche")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String,Object>> getMediaViaggio(@PathVariable Long viaggioId) {
        Map<String, Object> statistiche = viaggioService.getStatisticheRecensioni(viaggioId);

        return ResponseEntity.ok(statistiche);
    }

    @DeleteMapping(value = "/{viaggioId}/viaggio")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<Void> cancellaViaggio(@PathVariable Long viaggioId,@AuthenticationPrincipal UtenteLoggato organizzatore){

        log.warn("L'organizzatore {} sta tentando di eliminare il viaggio ID: {}",
                organizzatore.getUsername(), viaggioId);

        viaggioService.eliminaViaggio(viaggioId,organizzatore.getId());
        return ResponseEntity.ok().build();
    }
}
