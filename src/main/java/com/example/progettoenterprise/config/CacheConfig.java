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

    @Bean("cacheManager")
    public CacheManager cacheManager() {
        // Creazione della cache per le statistiche dei viaggi
        return new ConcurrentMapCacheManager(CACHE_VIAGGI_MEDIA);
    }
}