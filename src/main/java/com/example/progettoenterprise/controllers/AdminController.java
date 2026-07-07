package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.repositories.specifications.RichiestaPromozioneSpecification;
import com.example.progettoenterprise.data.service.AdminService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import com.example.progettoenterprise.security.ratelimiter.RateLimitPolicy;
import com.example.progettoenterprise.security.ratelimiter.WithRateLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/richieste")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<Page<RichiestaPromozioneDTO>> getRichieste(
            @RequestParam(required = false) String stato,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Admin {} sta cercando richieste (pag: {}) con stato: {}, username: {}",
                utenteLoggato.getUsername(), page, stato, username);

        RichiestaPromozioneSpecification.RichiestaFilter filter = new RichiestaPromozioneSpecification.RichiestaFilter();

        if (stato != null && !stato.isBlank()) {
            try {
                filter.setStato(com.example.progettoenterprise.data.entities.RichiestaPromozione.StatoRichiesta.valueOf(stato));
            } catch (IllegalArgumentException e) {
                log.warn("Stato non valido: {}", stato);
            }
        }

        if (username != null && !username.isBlank()) {
            filter.setUsernameViaggiatore(username);
        }

        Page<RichiestaPromozioneDTO> richieste = adminService.getRichiestePaginate(filter, page, size);
        return ResponseEntity.ok(richieste);
    }

    @PostMapping("/{id}/approva")
    @WithRateLimit(RateLimitPolicy.CRITICAL)
    public ResponseEntity<String> approvaRichiesta(
            @PathVariable Long id,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.info("Richiesta di approvazione della richiesta {}", id);
        adminService.approvaRichiesta(id, utenteLoggato.getId());
        return ResponseEntity.ok("Richiesta approvata con successo");
    }

    @PostMapping("/{id}/rifiuta")
    @WithRateLimit(RateLimitPolicy.CRITICAL)
    public ResponseEntity<String> rifiutaRichiesta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {
        log.info("Richiesta di rifiuto della richiesta {}", id);
        String note = (String) payload.get("noteAdmin");
        Long adminId = utenteLoggato.getId();
        adminService.rifiutaRichiesta(id, note, adminId);
        return ResponseEntity.ok("Richiesta rifiutata correttamente.");
    }

    @PostMapping("/utenti/{userId}/ban")
    @WithRateLimit(RateLimitPolicy.CRITICAL)
    public ResponseEntity<String> banUtente(@PathVariable Long userId) {
        adminService.banUtente(userId);
        return ResponseEntity.ok("Utente bannato con successo");
    }

    @GetMapping("/utenti-bannati")
    public ResponseEntity<List<UtenteDTO>> getUtentiBannati() {
        log.info("Richiesta lista utenti bannati");
        return ResponseEntity.ok(adminService.getUtentiBannati());
    }

    @GetMapping("/utenti-bannati/paginati")
    public ResponseEntity<Page<UtenteDTO>> getUtentiBannatiPaginati(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String ricerca) {

        log.info("Richiesta utenti bannati paginati - page: {}, size: {}, ricerca: {}", page, size, ricerca);
        Page<UtenteDTO> utentiPage = adminService.getUtentiBannatiPaginati(page, size, ricerca);
        return ResponseEntity.ok(utentiPage);
    }

    @PutMapping("/utenti/{id}/riattiva")
    @WithRateLimit(RateLimitPolicy.CRITICAL)
    public ResponseEntity<Void> riattivaUtente(@PathVariable Long id) {
        log.info("L'amministratore ha richiesto la riattivazione dell'utente ID: {}", id);
        adminService.sbannaUtente(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/promozioni/{id}/documento")
    public ResponseEntity<Resource> scaricaDocumentoCandidatura(@PathVariable Long id) {
        Resource file = adminService.scaricaDocumentoCandidatura(id);
        String contentType = "application/octet-stream";
        String fileName = file.getFilename();
        if (fileName.endsWith(".pdf")) contentType = "application/pdf";
        else if (fileName.endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else if (fileName.endsWith(".doc")) contentType = "application/msword";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(file);
    }
}