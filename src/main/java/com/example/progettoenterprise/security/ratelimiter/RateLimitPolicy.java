package com.example.progettoenterprise.security.ratelimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import java.time.Duration;

public enum RateLimitPolicy {
    // Massimo 4 richieste ogni 10 secondi per le operazioni critiche
    CRITICAL (Bandwidth.classic(4, Refill.intervally(4, Duration.ofSeconds(10)))),

    // Massimo 40 richieste al minuto per le letture standard
    READ_STANDARD (Bandwidth.classic(40, Refill.intervally(40, Duration.ofMinutes(1)))),

    // Massimo 100 richieste al minuto come fallback globale
    DEFAULT (Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))));

    private final Bandwidth limit;

    RateLimitPolicy(Bandwidth limit) {
        this.limit = limit;
    }

    public Bandwidth getLimit() {
        return limit;
    }

    // Fallback
    public static RateLimitPolicy fallbackPolicy(String metodo) {
        if ("GET".equalsIgnoreCase(metodo)) {
            return READ_STANDARD;
        }
        return DEFAULT;
    }
}