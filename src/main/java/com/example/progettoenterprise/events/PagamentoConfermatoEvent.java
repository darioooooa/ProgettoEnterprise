package com.example.progettoenterprise.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagamentoConfermatoEvent {
    private final String tokenViaggiatore;
    private final String tokenOrganizzatore;
    private final String destinazioneViaggio;
    private final String usernameViaggiatore;
}
