package com.example.progettoenterprise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Interceptor verifica il token jwt
    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // canale di uscita: i client si connetteranno a questo canale per ricevere le notifiche
        config.enableSimpleBroker("/topic");

        // Canale di entrata: i messaggi inviati dal frontend dovranno iniziare con questo prefisso
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // L'endpoint fisico a cui Angular si collegherà per avviare la connessione WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200") ;
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Ogni volta che arriva un messaggio dal frontend (Client Inbound) fallo passare prima dentro  JwtChannelInterceptor"
        registration.interceptors(jwtChannelInterceptor);
    }
}