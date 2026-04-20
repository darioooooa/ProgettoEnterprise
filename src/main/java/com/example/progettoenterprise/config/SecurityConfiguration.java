package com.example.progettoenterprise.config;

import com.example.progettoenterprise.security.RequestFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Classe che configura la sicurezza dell'applicazione
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    // Filtro che controlla i JWT nelle richieste HTTP
    private final RequestFilter requestFilter;

    // Bean usato per codificare le password
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Bean usato per ottenere l'AuthenticationManager da Spring Security
    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Metodo che definisce la catena di filtri di sicurezza
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Abilitazione della CORS
                .cors(Customizer.withDefaults())
                // Regole di accesso agli URL
                .authorizeHttpRequests( auth -> {
                    // Endpoint accessibili a chiunque
                    auth.requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll();
                    // Endpoint accessibili solo agli utenti autenticati
                    auth.anyRequest().authenticated();
                })
                // Disabilitazione della protezione CSRF, sicuro perchè usiamo JWT
                .csrf(csrf_ -> csrf_.disable())
                // Impostazione della policy di creazione della sessione su STATELESS
                .sessionManagement(mng -> mng.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Aggiunta del filtro personalizzato prima del filtro di autenticazione,
                // così che i JWT vengano verificati prima di tutto
                .addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
