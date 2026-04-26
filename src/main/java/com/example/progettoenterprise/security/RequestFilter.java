package com.example.progettoenterprise.security;

import com.example.progettoenterprise.serviceImpl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Intercetta le richieste HTTP in entrata
// Estende OncePerRequestFilter per garantire una singola esecuzione per ogni richiesta
@Component
@RequiredArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenStore tokenStore;

    // Metodo che viene eseguito per ogni richiesta HTTP in entrata
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // Estrae il token dall'intestazione della richiesta HTTP
        String token = tokenStore.getToken(request);

        // Se il token è valido, crea un token di autenticazione e lo imposta nel SecurityContext
        if (token != null) {
            try{
                // Validazione del token
                String username = tokenStore.getUser(token);

                // Carica i dettagli dell'utente dal database
                UserDetails user = userDetailsService.loadUserByUsername(username);

                // Creazione del token di autenticazione
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities() );

                // Impostazione dei dettagli dell'autenticazione'
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Autenticazione dell'utente con il token di autenticazione
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // Token non valido
                // Spring Security bloccherà la richiesta più avanti se la rotta è protetta
            }
        }
        // Esegue la richiesta HTTP successiva
        chain.doFilter(request, response);
    }
}
