package com.example.progettoenterprise.config.filter;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Admin;
import com.example.progettoenterprise.data.entities.Organizzatore;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.security.UtenteLoggato;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserSyncFilter extends OncePerRequestFilter {

    private final UtenteRepository utenteRepository;
    private final MessageLang messageLang;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Controlla se c'è un utente autenticato da keycloak tramite JWT
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {

            // Estrae i dati dal token di keycloak
            String email = jwtAuth.getToken().getClaimAsString("email");
            String username = jwtAuth.getToken().getClaimAsString("preferred_username");
            String nome = jwtAuth.getToken().getClaimAsString("given_name");
            String cognome = jwtAuth.getToken().getClaimAsString("family_name");
            Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();

            if (email == null || email.trim().isEmpty()) {
                log.error("Email non trovata nel token di keycloak");
                throw new IllegalArgumentException(messageLang.getMessage("auth.keycloak.email_not_found"));
            }
            if (username == null || username.trim().isEmpty()) {
                log.error("Username non trovato nel token di keycloak");
                throw new IllegalArgumentException(messageLang.getMessage("auth.keycloak.username_not_found"));
            }

            // Cerca l'utente nel database del progetto
            Optional<Utente> utenteOpt = utenteRepository.findByEmail(email);
            Utente utenteDb;

            if (utenteOpt.isPresent()) {
                utenteDb = utenteOpt.get();
            } else {
                // Se non esiste, lo si crea
                log.info("Nuovo utente da keycloak rilevato. Creazione nel database locale: {}", email);

                boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                boolean isOrg = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZZATORE"));

                if (isAdmin) {
                    Admin admin = new Admin();
                    admin.setRuolo(Utente.Ruolo.ROLE_ADMIN);
                    utenteDb = admin;
                } else if (isOrg) {
                    Organizzatore org = new Organizzatore();
                    org.setRuolo(Utente.Ruolo.ROLE_ORGANIZZATORE);
                    utenteDb = org;
                } else {
                    Viaggiatore viag = new Viaggiatore();
                    viag.setRuolo(Utente.Ruolo.ROLE_VIAGGIATORE);
                    utenteDb = viag;
                }

                utenteDb.setEmail(email);
                utenteDb.setUsername(username);
                utenteDb.setNome(nome);
                utenteDb.setCognome(cognome);
                utenteDb.setAttivo(true);

                utenteDb = utenteRepository.save(utenteDb);
            }

            // Crea UtenteLoggato, utile ai controller per sapere chi è l'utente loggato
            UtenteLoggato utenteLoggato = new UtenteLoggato(
                    utenteDb.getId(),
                    utenteDb.getUsername(),
                    authorities
            );

            // Sostituisce il JWT generico con il nostro utente personalizzato all'interno di spring
            UsernamePasswordAuthenticationToken customAuth = new UsernamePasswordAuthenticationToken(
                    utenteLoggato, null, authorities
            );
            SecurityContextHolder.getContext().setAuthentication(customAuth);

        }

        // Va avanti con la catena dei filtri
        filterChain.doFilter(request, response);
    }
}