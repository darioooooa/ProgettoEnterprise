package com.example.progettoenterprise.security.ratelimiter;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitStorageService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket getBucketForClient(String identityKey, String uri, String metodo, RateLimitPolicy policy) {
        // Genera una chiave univoca combinando utente, metodo e rotta
        String uniqueCacheKey = identityKey + ":" + metodo + ":" + uri;

        // Se il bucket per questa rotta specifica non esiste, lo crea usando la policy ricevuta
        return cache.computeIfAbsent(uniqueCacheKey, k ->
                Bucket.builder().addLimit(policy.getLimit()).build()
        );
    }
}