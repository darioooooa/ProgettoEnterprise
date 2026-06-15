package com.example.progettoenterprise.config;

import com.example.progettoenterprise.exception.ratelimiter.ManyRequestException;
import com.example.progettoenterprise.security.ratelimiter.RateLimitPolicy;
import com.example.progettoenterprise.security.ratelimiter.RateLimitStorageService;
import io.github.bucket4j.Bucket;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final RateLimitStorageService rateLimitStorageService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Intercettiamo solo il comando di CONNECT ovvero quando il client si collega
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                try {
                    // Decodifico il token JWT
                    Jwt jwt = jwtDecoder.decode(token);

                    // Estraiamo l'utente e i suoi ruoli
                    String username = jwt.getClaimAsString("preferred_username");

                    // Creiamo l'oggetto di autenticazione per Spring Security
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, null);

                    // Iniettiamo l'utente nel contesto del pacchetto WebSocket
                    accessor.setUser(authentication);

                } catch (Exception e) {
                    System.out.println("Errore di autenticazione sul WebSocket: Token non valido o scaduto.");
                }
            }
        }
        // Gestione del rate limiting sulla chat (in fase di invio)
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            // Recupera l'utente associato a questa sessione WebSocket
            String username = accessor.getUser() != null ? accessor.getUser().getName() : null;
            String destinazione = accessor.getDestination();

            if (username != null && destinazione != null) {
                // Policy CRITICAL
                Bucket bucket = rateLimitStorageService.getBucketForClient(username, destinazione, "SEND", RateLimitPolicy.DEFAULT);

                if (!bucket.tryConsume(1)) {
                    // Se esaurisce i token, viene lanciata un'eccezione bloccante sul canale WebSocket
                    throw new ManyRequestException("Stai inviando messaggi troppo velocemente! Rallenta.");
                }
            }
        }

        return message;
    }
}