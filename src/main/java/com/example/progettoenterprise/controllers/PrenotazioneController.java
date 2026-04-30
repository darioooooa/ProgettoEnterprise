package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.service.PrenotazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneController {
    @Autowired
    private PrenotazioneService prenotazioneService;

    @GetMapping("/{prenotazioneId}/esporta-calendario")
    public ResponseEntity<byte[]> scaricaFileCalendario(@PathVariable Long prenotazioneId) {

        byte[] datiCalendario = prenotazioneService.esportaPrenotazioni(prenotazioneId);

        HttpHeaders intestazioni = new HttpHeaders();
        String nomeFile = "prenotazione_" + prenotazioneId + ".ics";
        intestazioni.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeFile);
        intestazioni.add(HttpHeaders.CONTENT_TYPE, "text/calendar");

        return new ResponseEntity<>(datiCalendario, intestazioni, HttpStatus.OK);
    }
}
