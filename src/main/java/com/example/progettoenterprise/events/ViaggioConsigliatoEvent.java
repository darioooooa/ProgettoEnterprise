package com.example.progettoenterprise.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViaggioConsigliatoEvent {
    private final String tokenViaggiatore;
    private final String citta;
    private final String titoloNuovoViaggio;
}