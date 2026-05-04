package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/viaggi", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ViaggioController {
    private final ViaggioService viaggioService;

    // Endpoint per creare un nuovo viaggio (organizzatore)
    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> creaViaggio(
            @Valid @RequestBody ViaggioDTO viaggioDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Recupera lo username dell'utente autenticato
        String username = userDetails.getUsername();
        System.out.println(username);

        ViaggioDTO viaggioCreato = viaggioService.creaViaggio(viaggioDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(viaggioCreato);
    }

    // Endpoint per ottenere le statistiche delle recensioni di un viaggio
    @GetMapping(value = "/{viaggioId}/statistiche")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMediaViaggio(@PathVariable Long viaggioId) {
        Map<String, Object> statistiche = viaggioService.getStatisticheRecensioni(viaggioId);

        return ResponseEntity.ok(statistiche);
    }
    @DeleteMapping(value = "/{viaggioId}/viaggio")
    public ResponseEntity<?> cancellaViaggio(@PathVariable Long viaggioId,@RequestParam Long organizzatoreId){
        viaggioService.eliminaViaggio(viaggioId,organizzatoreId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/organizzatore/{organizzatoreId}")
    public ResponseEntity<List<ViaggioDTO>> getViaggiPerOrganizzatore(@PathVariable Long organizzatoreId){
        List<ViaggioDTO> viaggi = viaggioService.getViaggiPerOrganizzatore(organizzatoreId);
        return ResponseEntity.ok(viaggi);
    }
}
