package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.PagamentoService;
import com.example.progettoenterprise.dto.PagamentoDTO;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import com.example.progettoenterprise.security.UtenteLoggato;
import com.example.progettoenterprise.security.ratelimiter.RateLimitPolicy;
import com.example.progettoenterprise.security.ratelimiter.WithRateLimit;
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
@RequestMapping("/api/v1/pagamento")
@RequiredArgsConstructor
@Slf4j
public class PagamentoController {

    private final PagamentoService pagamentoService;
    @PostMapping("/crea-intent/{idPrenotazione}")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<Map<String,String>> creaPaymentIntent(@PathVariable Long idPrenotazione,
                                                          @AuthenticationPrincipal UtenteLoggato utenteLoggato)throws Exception{
        log.info("L'utente {} sta avviando il pagamento per la prenotazione ID: {}",utenteLoggato.getUsername(), idPrenotazione);
        // il service che contatta Stripe
        String clientSecret = pagamentoService.creaPaymentIntent(idPrenotazione, utenteLoggato.getId());
        // Restituiamo un oggetto JSON con il contenutp dentro, pronto per Angular
        return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
    }

    @PostMapping("/conferma")
    @PreAuthorize("hasRole('VIAGGIATORE')")
    public ResponseEntity<PrenotazioneDTO> confermaPagamento(
            @RequestBody PagamentoDTO pagamentoDTO,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta confermando il pagamento per la prenotazione ID: {}",
                utenteLoggato.getUsername(), pagamentoDTO.getIdPrenotazione());

        // Salviamo la ricevuta e aggiorniamo la prenotazione nel suo relativo stato
        PrenotazioneDTO prenotazioneAggiornata = pagamentoService.confermaPagamento(pagamentoDTO, utenteLoggato.getId());

        return ResponseEntity.ok(prenotazioneAggiornata);
    }

    @PostMapping("/rimborsa/{idPrenotazione}")
    @PreAuthorize("hasRole('ORGANIZZATORE')")
    public ResponseEntity<?> rimborsaPrenotazione(@PathVariable Long idPrenotazione) {

        log.info("Richiesta di rimborso ricevuta per la prenotazione ID: {}", idPrenotazione);

        try {
            // Chiamiamo il service che dialoga con Stripe
            pagamentoService.rimborsaPrenotazione(idPrenotazione);


            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rimborso elaborato con successo su Stripe. Stato prenotazione aggiornato."
            ));

        } catch (Exception e) {
            log.error("Errore durante il rimborso per la prenotazione {}: ", idPrenotazione, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Impossibile completare il rimborso: " + e.getMessage()
            ));
        }
    }




}