package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
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
@RequestMapping(value = "/api/v1/viaggi", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class ViaggioController {
    private final ViaggioService viaggioService;

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
    //scelto un mapping senza id per evitare possibili metodi per poter entrare nella lista viaggi di un organizzatore
    @GetMapping("/miei-viaggi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ViaggioDTO>> getMieiViaggi(@AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        List<ViaggioDTO> viaggi = viaggioService.getViaggiPerOrganizzatore(utenteLoggato.getId());

        return ResponseEntity.ok(viaggi);
    }
}
