package com.example.progettoenterprise.config;

import com.example.progettoenterprise.config.filter.KeycloakUserSyncFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final String ROLE_PREFIX = "ROLE_";
    private final KeycloakUserSyncFilter keycloakUserSyncFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Abilitazione della CORS
                .cors(Customizer.withDefaults())
                // Disabilitazione della protezione CSRF, sicuro perchè usiamo JWT
                .csrf(csrf -> csrf.disable())
                // Impostazione della policy di creazione della sessione su STATELESS
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Regole di accesso agli URL
                .authorizeHttpRequests(auth -> auth
                        // Endpoint accessibili a chiunque
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        // Endpoint accessibili solo dagli utenti autenticati (richiede il JWT)
                        .anyRequest().authenticated()
                )
                // Spring fa da resource server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterAfter(keycloakUserSyncFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    // Converte il token JWT in un oggetto Authentication, includendo ruoli realm e client
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            // Estrae i ruoli globali
            extractRolesFromRealmAccess(jwt.getClaims(), authorities);
            // Estrae i ruoli specifici del client
            extractRolesFromResourceAccess(jwt.getClaims(), authorities);
            return authorities;
        });
        return converter;
    }

    // Estrazione dei ruoli
    private void extractRolesFromRealmAccess(Map<String, Object> claims, Set<GrantedAuthority> authorities) {
        Map<String, Object> realmAccess = getMap(claims, "realm_access");
        if (realmAccess != null) {
            List<String> roles = getList(realmAccess, "roles");
            addRoles(roles, authorities);
        }
    }
    private void extractRolesFromResourceAccess(Map<String, Object> claims, Set<GrantedAuthority> authorities) {
        Map<String, Object> resourceAccess = getMap(claims, "resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().forEach(clientAccess -> {
                if (clientAccess instanceof Map) {
                    List<String> roles = getList((Map<String, Object>) clientAccess, "roles");
                    addRoles(roles, authorities);
                }
            });
        }
    }

    // Aggiunge una lista di ruoli alle authorities
    private void addRoles(List<String> roles, Set<GrantedAuthority> authorities) {
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase())));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMap(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getList(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof List ? (List<String>) value : null;
    }

    // Configurazione della CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}