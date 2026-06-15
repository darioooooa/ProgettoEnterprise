package com.example.progettoenterprise.security.ratelimiter;

import com.example.progettoenterprise.exception.ratelimiter.ManyRequestException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitStorageService storageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String metodo = request.getMethod();

        if (uri.contains("/v3/api-docs") || uri.contains("/swagger-ui") || uri.contains("/assets/")) {
            return true;
        }

        // Determina la politica usando l'annotazione sul metodo (reflection)
        RateLimitPolicy policy = RateLimitPolicy.fallbackPolicy(metodo); // Default iniziale

        // Di default si usa l'URI reale come stringa di risorsa
        String risorsaKey = uri;

        if (handler instanceof HandlerMethod handlerMethod) {
            // Cerca se sopra il metodo del controller c'è l'annotazione @WithRateLimit
            WithRateLimit annotazione = handlerMethod.getMethodAnnotation(WithRateLimit.class);
            if (annotazione != null) {
                policy = annotazione.value();
            }

            // Estrae il pattern astratto di Spring (es. /api/v1/viaggi/{viaggioId}/immagini/{immagineId})
            String patternRotta = (String) request.getAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (patternRotta != null) {
                risorsaKey = patternRotta; // Sostituisce l'URI reale con il pattern astratto per accorpare i limiti
            }
        }

        String identityKey = recuperaIdentitaClient(request);
        Bucket tokenBucket = storageService.getBucketForClient(identityKey, risorsaKey, metodo, policy);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            throw new ManyRequestException("Quota di richieste esaurita per questa operazione. Riprova tra " + waitForRefill + " secondi.");
        }
    }

    private String recuperaIdentitaClient(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return "ANON-" + ipAddress;
    }
}