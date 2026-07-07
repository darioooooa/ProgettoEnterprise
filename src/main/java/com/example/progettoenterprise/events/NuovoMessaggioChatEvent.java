package com.example.progettoenterprise.events;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NuovoMessaggioChatEvent {
    private final String tokenRicevente;
    private final String mittenteUsername;
}
