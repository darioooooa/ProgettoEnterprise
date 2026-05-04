package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.AttivitaViaggioService;
import com.example.progettoenterprise.dto.AttivitaViaggioDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/viaggi/{viaggioId}/attivita-viaggi")
@RequiredArgsConstructor
public class AttivitaViaggioController {
    private final AttivitaViaggioService attivitaViaggioService;

    @PostMapping
    public ResponseEntity<AttivitaViaggioDTO> creaAttivitaViaggio(@PathVariable Long viaggioId,@Valid @RequestBody AttivitaViaggioDTO attivitaViaggioDTO){
        AttivitaViaggioDTO nuovaAttivita = attivitaViaggioService.creaAttivita(attivitaViaggioDTO);
        return new ResponseEntity<>(nuovaAttivita, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AttivitaViaggioDTO> GetAttivitaById(@PathVariable Long viaggioId,@PathVariable Long id) {
        return ResponseEntity.ok(attivitaViaggioService.getAttivitaById(id));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<AttivitaViaggioDTO>> getTimelineSpostamenti(@PathVariable Long viaggioId){
        return ResponseEntity.ok(attivitaViaggioService.getTimelineSpostamenti(viaggioId));
    }
    @GetMapping("/cerca")
    public ResponseEntity<List<AttivitaViaggioDTO>> cercaInViaggio(
            @PathVariable Long viaggioId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(attivitaViaggioService.cercaInViaggio(viaggioId, keyword));
    }

    @PutMapping("/{id}/modifica")
    public ResponseEntity<AttivitaViaggioDTO> modificaAttivita(
            @PathVariable Long id,
            @Valid @RequestBody AttivitaViaggioDTO dto) {
        return ResponseEntity.ok(attivitaViaggioService.modificaAttivitaViaggio(id, dto));
    }

    @GetMapping("/filtra-per-budget")
    public ResponseEntity<List<AttivitaViaggioDTO>> filtraPerBudget(
            @PathVariable Long viaggioId,
            @RequestParam Double budgetMax) {
        return ResponseEntity.ok(attivitaViaggioService.filtraPerBudget(viaggioId, budgetMax));
    }

    @DeleteMapping("/{attivitaId}")
    public ResponseEntity<?> cancellaAttivitaViaggio(@PathVariable Long attivitaId,@PathVariable Long viaggioId){
        attivitaViaggioService.eliminaAttivitaViaggio(attivitaId,viaggioId);
        return ResponseEntity.ok().build();
    }
}
