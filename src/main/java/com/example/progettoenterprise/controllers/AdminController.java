package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.service.AdminService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
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
@RequestMapping("/api/admin/richieste")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<RichiestaPromozioneDTO>> getRichiestePendenti(){
        List<RichiestaPromozioneDTO> richieste = adminService.getRichieste();
        log.info("Richieste di promozione: {}", richieste);
        return ResponseEntity.ok(richieste);
    }

    @PostMapping("/{id}/approva")
    public ResponseEntity<String> approvaRichiesta(@PathVariable Long id, @AuthenticationPrincipal UtenteLoggato utenteLoggato){
        log.info("Richiesta di approvazione della richiesta {}", id);
        adminService.approvaRichiesta(id, utenteLoggato.getId());
        return ResponseEntity.ok("Richiesta approvata con successo");
    }

    @PostMapping("/{id}/rifiuta")
    public ResponseEntity<String> rifiutaRichiesta(@PathVariable Long id, @RequestParam Long adminIdCorrente, @RequestBody Map<String, String> payload) {
        log.info("Richiesta di rifiuto della richiesta {}", id);
        String note = payload.get("noteAdmin");
        adminService.rifiutaRichiesta(id, note,adminIdCorrente);
        return ResponseEntity.ok("Richiesta rifiutata correttamente.");
    }

    @PostMapping("/utenti/{userId}/ban")
    public ResponseEntity<String> banUtente(
            @PathVariable Long userId) {

        adminService.banUtente(userId);
        return ResponseEntity.ok("Utente bannato con successo");
    }

    @GetMapping("/utenti-bannati")
    public ResponseEntity<List<UtenteDTO>> getUtentiBannati() {
        log.info("Richiesta lista utenti bannati");
        return ResponseEntity.ok(adminService.getUtentiBannati());
    }

    @PutMapping("/utenti/{id}/riattiva")
    public ResponseEntity<Void> riattivaUtente(@PathVariable Long id) {
        log.info("L'amministratore ha richiesto la riattivazione dell'utente ID: {}", id);
        adminService.sbannaUtente(id);
        return ResponseEntity.ok().build();
    }
}
