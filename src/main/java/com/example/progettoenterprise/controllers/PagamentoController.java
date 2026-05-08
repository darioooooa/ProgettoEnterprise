package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.PagamentoService;
import com.example.progettoenterprise.dto.PagamentoDTO;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pagamenti")
@RequiredArgsConstructor
@Slf4j
public class PagamentoController {

    private final PagamentoService pagamentoService;

    // 1. AGGIUNGI UNA NUOVA CARTA (201 Created)
    @PostMapping("/aggiungi")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<PagamentoDTO> aggiungiCarta(
            @RequestBody PagamentoDTO pagamentoDTO,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta aggiungendo un metodo di pagamento", utenteLoggato.getUsername());

        // Sicurezza assoluta: ignoriamo l'ID che arriva da fuori e usiamo quello del token
        pagamentoDTO.setIdViaggiatore(utenteLoggato.getId());

        PagamentoDTO salvato = pagamentoService.aggiungiCarta(pagamentoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvato);
    }

    // 2. VISUALIZZA LE PROPRIE CARTE (200 OK)
    @GetMapping("/mie-carte")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<List<PagamentoDTO>> getMieCarte(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero metodi di pagamento per l'utente: {}", utenteLoggato.getUsername());
        List<PagamentoDTO> carte = pagamentoService.getCarteViaggiatore(utenteLoggato.getId());
        return ResponseEntity.ok(carte);
    }

    @PostMapping("/checkout/{idPrenotazione}/{idMetodoPagamento}")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<PrenotazioneDTO> pagaPrenotazione(
            @PathVariable Long idPrenotazione,
            @PathVariable Long idMetodoPagamento,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta tentando di pagare la prenotazione ID: {} con la carta ID: {}",
                utenteLoggato.getUsername(), idPrenotazione, idMetodoPagamento);

        PrenotazioneDTO prenotazioneAggiornata = pagamentoService.pagaPrenotazione(
                idPrenotazione,
                idMetodoPagamento,
                utenteLoggato.getId()
        );

        return ResponseEntity.ok(prenotazioneAggiornata);
    }



    // 3. ELIMINA UNA CARTA (200 OK con messaggio JSON)
    @DeleteMapping("/{idPagamento}")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<?> eliminaCarta(
            @PathVariable Long idPagamento,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("L'utente {} sta eliminando la carta ID: {}", utenteLoggato.getUsername(), idPagamento);

        pagamentoService.eliminaCarta(idPagamento);
        return ResponseEntity.ok(Map.of("message", "Metodo di pagamento eliminato con successo"));
    }
}