package com.example.progettoenterprise.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PromemoriaViaggioEvent {

    private final String fcmToken;
    private final String titolo;
    private final String messaggio;


    public PromemoriaViaggioEvent(String fcmToken, String titolo, String messaggio) {
        this.fcmToken = fcmToken;
        this.titolo = titolo;
        this.messaggio = messaggio;
    }
}