package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ViaggiatoreService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/richieste-promozione")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<RichiestaPromozioneDTO> inviaRichiestaPromozione(
            @Valid @RequestBody RichiestaPromozioneDTO richiesta,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.info("Richiesta di invio di una nuova richiesta di promozione per l'utente ID: {}", utenteLoggato.getId());
        RichiestaPromozioneDTO nuovaRichiesta = viaggiatoreService.creaRichiestaPromozione(utenteLoggato.getId(), richiesta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaRichiesta);
    }


}


