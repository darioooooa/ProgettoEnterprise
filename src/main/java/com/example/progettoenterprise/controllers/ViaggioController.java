package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import com.example.progettoenterprise.dto.ViaggioMappaDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import com.example.progettoenterprise.security.ratelimiter.RateLimitPolicy;
import com.example.progettoenterprise.security.ratelimiter.WithRateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    @WithRateLimit(RateLimitPolicy.CRITICAL)
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

    @DeleteMapping(value = "/{viaggioId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<Void> eliminaViaggio(@PathVariable Long viaggioId,@AuthenticationPrincipal UtenteLoggato organizzatore){

        log.warn("L'organizzatore {} sta tentando di eliminare il viaggio ID: {}",
                organizzatore.getUsername(), viaggioId);

        viaggioService.eliminaViaggio(viaggioId,organizzatore.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mappa-viaggi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ViaggioMappaDTO>> getViaggiPerMappa(@AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        List<ViaggioMappaDTO> viaggiMappa = viaggioService.getViaggiMappa(utenteLoggato.getId());
        return ResponseEntity.ok(viaggiMappa);
    }

    @GetMapping("/{viaggioId}")
    @PreAuthorize("isAuthenticated()")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<ViaggioDTO> getViaggioById(@PathVariable Long viaggioId,@AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.info("Richiesta dettagli completi for il viaggio ID: {}", viaggioId);

        return ResponseEntity.ok(viaggioService.getViaggioById(viaggioId, utenteLoggato.getId()));
    }

    @GetMapping("/organizzatore/{organizzatoreId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ViaggioDTO>> getViaggiByOrganizzatore(@PathVariable Long organizzatoreId, @AuthenticationPrincipal UtenteLoggato utente) {
        log.info("Richiesta dei viaggi da parte dell'utente con id {}, per l'organizzatore con id {}", utente.getId(), organizzatoreId);
        return ResponseEntity.ok(viaggioService.getViaggiByOrganizzatore(organizzatoreId));
    }


    @PutMapping("/{viaggioId}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<ViaggioDTO> modificaViaggio(
            @PathVariable Long viaggioId,
            @Valid @RequestBody ViaggioDTO viaggioDTO,
            @AuthenticationPrincipal UtenteLoggato organizzatore) {

        log.info("L'organizzatore {} sta modificando il viaggio ID: {}", organizzatore.getUsername(), viaggioId);

        // Chiamata al metodo del Service passando l'ID del viaggio, il DTO e l'ID dell'utente loggato
        ViaggioDTO viaggioAggiornato = viaggioService.modificaViaggio(viaggioId, viaggioDTO, organizzatore.getId());

        return ResponseEntity.ok(viaggioAggiornato);
    }

    @GetMapping("/consigliati")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ViaggioDTO>> getViaggiConsigliati(@AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.info("L'utente {} sta richiedendo i viaggi consigliati", utenteLoggato.getUsername());
        List<ViaggioDTO> consigliati = viaggioService.getConsigliatiPerUtente(utenteLoggato.getId());
        return ResponseEntity.ok(consigliati);
    }

}