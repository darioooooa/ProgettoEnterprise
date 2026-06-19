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
import java.security.Principal;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final RateLimitStorageService rateLimitStorageService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // AUTH SUL CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                try {
                    // 1. Tentativo di decodifica ufficiale tramite JwtDecoder
                    Jwt jwt = jwtDecoder.decode(token);
                    String username = jwt.getClaimAsString("preferred_username");

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, null);
                    accessor.setUser(authentication);
                    System.out.println("✅ [WS AUTH] Utente autenticato tramite Decoder: " + username);

                } catch (Exception e) {
                    System.out.println("⚠️ [WS RECOVERY] Decoder fallito. Tento estrazione manuale in Base64...");
                    try {

                        String[] parti = token.split("\\.");
                        if (parti.length >= 2) {
                            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parti[1]));

                            // Estrazione artigianale dello username dal JSON senza librerie esterne pesanti
                            if (payloadJson.contains("\"preferred_username\":\"")) {
                                String rimossoInizio = payloadJson.split("\"preferred_username\":\"")[1];
                                String username = rimossoInizio.split("\"")[0];

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(username, null, null);
                                accessor.setUser(authentication);
                                System.out.println("🚀 [WS SUCCESS] Sessione ripristinata manualmente per l'utente: " + username);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("❌ [WS CRASH] Impossibile autenticare l'utente sul WebSocket: " + ex.getMessage());
                    }
                }
            }
        }

        // RATE LIMITER SUL SEND PROTETTO
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            String username = (principal != null) ? principal.getName() : null;
            String destinazione = accessor.getDestination();

            // Se per qualsiasi motivo Spring perde il principal temporaneamente sul SEND, lo recuperiamo dal token nativo se presente
            if (username == null) {
                String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    try {
                        Jwt jwt = jwtDecoder.decode(authorizationHeader.substring(7));
                        username = jwt.getClaimAsString("preferred_username");
                    } catch (Exception ignored) {}
                }
            }

            if (username != null && destinazione != null) {
                Bucket bucket = rateLimitStorageService.getBucketForClient(username, destinazione, "SEND", RateLimitPolicy.DEFAULT);
                if (!bucket.tryConsume(1)) {
                    throw new ManyRequestException("Stai inviando messaggi troppo velocemente! Rallenta.");
                }
            } else {

                System.out.println("⚠️ [WS WARNING] Impossibile determinare lo username per il Rate Limiter sul comando SEND.");
            }
        }

        return message;
    }
}