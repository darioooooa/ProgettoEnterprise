package com.example.progettoenterprise.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configurazione della cache
@Configuration
@EnableCaching
public class CacheConfig {
    // Nome della cache per le statistiche dei viaggi
    public static final String CACHE_VIAGGI_MEDIA = "viaggi_media";

    // Nome della cache per l'autenticazione degli utenti
    public static final String CACHE_UTENTI_AUTH = "utenti_auth";

    @Bean("cacheManager")
    public CacheManager cacheManager() {
        // Creazione della cache per le statistiche dei viaggi e l'autenticazione
        return new ConcurrentMapCacheManager(CACHE_VIAGGI_MEDIA, CACHE_UTENTI_AUTH);
    }
}