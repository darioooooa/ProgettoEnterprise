package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ViaggiatoreService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import com.example.progettoenterprise.security.ratelimiter.RateLimitPolicy;
import com.example.progettoenterprise.security.ratelimiter.WithRateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/viaggiatori") // Rotta base per tutto ciò che riguarda i viaggiatori
@RequiredArgsConstructor
@Slf4j
public class ViaggiatoreController {
    private final ViaggiatoreService viaggiatoreService;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ViaggiatoreDTO> getProfilo(@PathVariable Long id) {
        log.info("Richiesta visualizzazione profilo per il viaggiatore ID: {}", id);
        return ResponseEntity.ok(viaggiatoreService.getProfiloViaggiatore(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ViaggiatoreDTO> update(@PathVariable Long id, @RequestBody ViaggiatoreDTO dto) {
        log.info("Richiesta aggiornamento profilo per il viaggiatore ID: {}", id);
        return ResponseEntity.ok(viaggiatoreService.aggiornaProfilo(id, dto));
    }
    @GetMapping("/cerca")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Iterable<ViaggiatoreDTO>> cercaViaggiatori(@RequestParam String query) {
        log.info("Richiesta ricerca per: {}", query);
        return ResponseEntity.ok(viaggiatoreService.cercaViaggiatori(query));
    }

    @PostMapping(value = "/richieste-promozione", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VIAGGIATORE')")
    @WithRateLimit(RateLimitPolicy.CRITICAL)
    public ResponseEntity<RichiestaPromozioneDTO> inviaRichiestaPromozione(
            @RequestPart("richiesta") @Valid RichiestaPromozioneDTO richiesta,
            @RequestPart(value = "file") MultipartFile file,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Richiesta di invio di una nuova richiesta di promozione con file per l'utente ID: {}", utenteLoggato.getId());
        RichiestaPromozioneDTO nuovaRichiesta = viaggiatoreService.creaRichiestaPromozione(utenteLoggato.getId(), richiesta, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaRichiesta);
    }

    @GetMapping("/{id}/richiesta-pendente")
    public ResponseEntity<?> getRichiesta(@PathVariable Long id) {
        return ResponseEntity.ok(viaggiatoreService.trovaRichiestaPendente(id));
    }


}


