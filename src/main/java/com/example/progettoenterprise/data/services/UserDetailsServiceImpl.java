package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.config.i18n.MessageLang;
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
    private final MessageLang messageLang;

    // Metodo chiamato automaticamente dal framework per ottenere i dati dell'utente
    @Override
    public UserDetails loadUserByUsername(String identificativo) throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByUsernameOrEmail(identificativo,identificativo)
                .orElseThrow(() -> new UsernameNotFoundException(messageLang.getMessage("auth.user.notfound", identificativo)));

        return new User(
                utente.getUsername(),
                utente.getPassword(),
                // ROLE_ è lo standard spring per i ruoli
                List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name())));
    }
}
