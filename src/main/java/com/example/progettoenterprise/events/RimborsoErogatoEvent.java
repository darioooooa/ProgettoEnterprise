package com.example.progettoenterprise.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RimborsoErogatoEvent {
    private final String tokenViaggiatore;
    private final String nomeViaggio;
}
