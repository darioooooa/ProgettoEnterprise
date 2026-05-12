package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.NotificaSpecification;
import com.example.progettoenterprise.data.service.NotificaService;
import com.example.progettoenterprise.dto.NotificaDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/notifiche", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class NotificaController {

    private final NotificaService notificaService;

    //endpoint per inviare una notifica
    @PostMapping("/invia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificaDTO> inviaNotifica(
            @RequestParam Long utenteId,
            @RequestParam String messaggio,
            @RequestParam(required = false) Long idRiferimento,
            @AuthenticationPrincipal UtenteLoggato mittente) {

        log.info("L'utente {} (ID: {}) sta inviando una notifica via URL all'utente ID: {}",
                mittente.getUsername(), mittente.getId(),utenteId);
        NotificaDTO nuovaNotifica = notificaService.inviaNotifica(utenteId, messaggio, idRiferimento);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovaNotifica);
    }

    //Recupero tutte le notifiche dell'utente con i filtri
    @GetMapping("/utente/{utenteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificaDTO>> getNotifiche(
            NotificaSpecification.NotificaFilter filter,
            @PathVariable Long utenteId,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta consultando lo storico notifiche dell'ID: {}",
                utenteLoggato.getUsername(), utenteId);

        Page<NotificaDTO> notifiche = notificaService.getNotifiche(utenteId, filter, page);
        return ResponseEntity.ok(notifiche);
    }

    //Segno una notifica come letta
    @PatchMapping("/{id}/letta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> segnaComeLetta(
            @PathVariable Long id,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta segnando come letta la notifica ID: {}",
                utenteLoggato.getUsername(), id);

        notificaService.segnaComeLetta(id);
        return ResponseEntity.ok().build();
    }


    //endpoint per segnare tutte le notifiche come lette
    @PatchMapping("/utente/{utenteId}/leggi-tutte")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> segnaTutteComeLette(
            @PathVariable Long utenteId,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Reset notifiche (segna tutte come lette) per l'utente ID: {} richiesto da: {}",
                utenteId, utenteLoggato.getUsername());

        notificaService.segnaTutteComeLette(utenteId);
        return ResponseEntity.ok().build();
    }

    //Conteggio delle notifiche di un utente
    @GetMapping("/utente/{utenteId}/conteggio")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> conteggioNonLette(@PathVariable Long utenteId) {
        long count = notificaService.conteggioNotificheNonLette(utenteId);
        return ResponseEntity.ok(count);
    }

    // Endpoint per eliminare una notifica
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminaNotifica(
            @PathVariable Long id,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.warn("ELIMINAZIONE: L'utente {} sta cancellando la notifica ID: {}",
                utenteLoggato.getUsername(), id);

        notificaService.eliminaNotifica(id);
        return ResponseEntity.ok(Map.of("message", "Notifica eliminata con successo!"));
    }


}
