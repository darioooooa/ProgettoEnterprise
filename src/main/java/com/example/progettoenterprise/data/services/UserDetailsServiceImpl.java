package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Servzio che permette di caricare i dati dell'utente durante l'autenticazione
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UtenteRepository utenteRepository;

    // Metodo chiamato automaticamente dal framework per ottenere i dati dell'utente
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con username: " + username));

        return new User(
                utente.getUsername(),
                utente.getPassword(),
                // ROLE_ è lo standard spring per i ruoli
                List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name())));
    }
}
