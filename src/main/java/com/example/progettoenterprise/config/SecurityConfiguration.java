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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                // Disabilitazione della protezione CSRF, sicuro perchè usiamo JWT
                .csrf(csrf_ -> csrf_.disable())

                // Regole di accesso agli URL
                .authorizeHttpRequests( auth -> {
                    auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll();
                    // Endpoint accessibili a chiunque
                    auth.requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll();
                    // Endpoint accessibili solo agli utenti autenticati
                    auth.requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html").permitAll();
                    auth.anyRequest().authenticated();
                })


                // Impostazione della policy di creazione della sessione su STATELESS
                .sessionManagement(mng -> mng.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Aggiunta del filtro personalizzato prima del filtro di autenticazione,
                // così che i JWT vengano verificati prima di tutto
                .addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    //serve per evitare il CORS error. Serve per far comunicare il frontend con il backend
    //serve per far dare il permesso al browser di far passare la chiamata
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permetti al frontend Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Permetti tutti i metodi (GET, POST, PUT, DELETE, ecc.)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permetti tutti gli header (per il JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        // Permetti l'invio di credenziali (se servono)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
