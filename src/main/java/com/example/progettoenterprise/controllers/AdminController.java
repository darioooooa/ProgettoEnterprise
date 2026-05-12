package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.AdminService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/richieste")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<RichiestaPromozioneDTO>> getRichiestePendenti(){
        List<RichiestaPromozioneDTO> richieste = adminService.getRichieste();
        return ResponseEntity.ok(richieste);
    }

    @PostMapping("/{id}/approva")
    public ResponseEntity<String> approvaRichiesta(@PathVariable Long id,@RequestParam Long adminIdCorrente){
        adminService.approvaRichiesta(id,adminIdCorrente);
        return ResponseEntity.ok("Richiesta approvata con successo");
    }

    @PostMapping("/{id}/rifiuta")
    public ResponseEntity<String> rifiutaRichiesta(@PathVariable Long id, @RequestParam Long adminIdCorrente, @RequestBody Map<String, String> payload) {
        String note = payload.get("noteAdmin");
        adminService.rifiutaRichiesta(id, note,adminIdCorrente);
        return ResponseEntity.ok("Richiesta rifiutata correttamente.");
    }
}
