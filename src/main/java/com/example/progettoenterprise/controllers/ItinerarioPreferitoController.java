package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.ItinerarioPreferitoService;
import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
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
@RequestMapping("/api/v1/itinerari-preferiti")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class ItinerarioPreferitoController {

    private final ItinerarioPreferitoService itinerarioService;

    // CREA UNA NUOVA LISTA
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ItinerarioPreferitoDTO> creaLista(
            @RequestBody ItinerarioPreferitoDTO richiesta,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta creando una nuova lista di itinerari: {}",
                utenteLoggato.getUsername(), richiesta.getNome());

        ItinerarioPreferitoDTO salvata = itinerarioService.creaLista(richiesta, utenteLoggato.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvata);
    }

    // RECUPERA LE MIE LISTE PERSONALI
    @GetMapping("/mie-liste")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ItinerarioPreferitoDTO>> getMieListe(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero liste personali per l'utente: {}", utenteLoggato.getUsername());
        return ResponseEntity.ok(itinerarioService.getMieListe(utenteLoggato.getId()));
    }

    // RECUPERA LE LISTE CONDIVISE CON ME
    @GetMapping("/condivise-con-me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ItinerarioPreferitoDTO>> getListeCondivise(
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("Recupero liste condivise con l'utente: {}", utenteLoggato.getUsername());
        return ResponseEntity.ok(itinerarioService.getListeCondiviseConMe(utenteLoggato.getId()));
    }

    // RICERCA LISTE PUBBLICHE PER NOME
    @GetMapping("/ricerca-pubblica")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ItinerarioPreferitoDTO>> cercaListePubbliche(@RequestParam String nome) {
        log.info("Ricerca pubblica per liste con nome: {}", nome);
        return ResponseEntity.ok(itinerarioService.cercaListePubbliche(nome));
    }

    // DETTAGLIO LISTA (200 OK)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ItinerarioPreferitoDTO> getDettaglioLista(@PathVariable Long id) {
        log.info("Richiesta dettaglio per la lista ID: {}", id);
        return ResponseEntity.ok(itinerarioService.getListaById(id));
    }

    // CAMBIA VISIBILITÀ
    @PatchMapping("/{id}/visibilita")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ItinerarioPreferitoDTO> cambiaVisibilita(
            @PathVariable Long id,
            @RequestParam String nuovaVisibilita,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.info("L'utente {} sta cambiando la visibilità della lista {} in {}",
                utenteLoggato.getUsername(), id, nuovaVisibilita);

        return ResponseEntity.ok(itinerarioService.cambiaVisibilita(id, nuovaVisibilita, utenteLoggato.getId()));
    }

    // ELIMINA LISTA
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminaLista(
            @PathVariable Long id,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        log.warn("L'utente {} sta eliminando la lista ID: {}", utenteLoggato.getUsername(), id);
        itinerarioService.eliminaLista(id, utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Lista eliminata con successo"));
    }

    @PostMapping("/{idLista}/viaggi/{idViaggio}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> aggiungiViaggio(
            @PathVariable Long idLista,
            @PathVariable Long idViaggio,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        itinerarioService.aggiungiViaggioAllaLista(idLista, idViaggio, utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Viaggio aggiunto con successo all'itinerario!"));
    }

    @DeleteMapping("/{idLista}/viaggi/{idViaggio}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rimuoviViaggio(
            @PathVariable Long idLista,
            @PathVariable Long idViaggio,
            @AuthenticationPrincipal UtenteLoggato utenteLoggato) {

        itinerarioService.rimuoviViaggioDallaLista(idLista, idViaggio, utenteLoggato.getId());
        return ResponseEntity.ok(Map.of("message", "Viaggio rimosso con successo dall'itinerario!"));
    }
}