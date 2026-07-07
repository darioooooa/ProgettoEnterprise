package com.example.progettoenterprise.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SegnalazioneUtenteEvent {
    private final String tokenUtenteSegnalato;
}