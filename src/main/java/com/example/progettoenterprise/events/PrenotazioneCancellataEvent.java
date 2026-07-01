package com.example.progettoenterprise.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrenotazioneCancellataEvent {
    private final String tokenOrganizzatore;
    private final String usernameViaggiatore;
    private final String destinazioneViaggio;
}
