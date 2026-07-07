package com.example.progettoenterprise.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NuovaRecensioneEvent {
    private final String tokenOrganizzatore;
    private final String nomeViaggio;
    private final String usernameViaggiatore;
}
