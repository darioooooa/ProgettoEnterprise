package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ViaggiatoreService;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viaggiatori") // Rotta base per tutto ciò che riguarda i viaggiatori
@RequiredArgsConstructor
public class ViaggiatoreController {
    private final ViaggiatoreService viaggiatoreService;

    @GetMapping("/{id}")
    public ResponseEntity<ViaggiatoreDTO> getProfilo(@PathVariable Long id) {
        return ResponseEntity.ok(viaggiatoreService.getProfiloViaggiatore(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViaggiatoreDTO> update(@PathVariable Long id, @RequestBody ViaggiatoreDTO dto) {
        return ResponseEntity.ok(viaggiatoreService.aggiornaProfilo(id, dto));
    }
    @GetMapping("/cerca")
    public ResponseEntity<Iterable<ViaggiatoreDTO>> cercaViaggiatori(@RequestParam String query) {
        return ResponseEntity.ok(viaggiatoreService.cercaViaggiatori(query));
    }



}


