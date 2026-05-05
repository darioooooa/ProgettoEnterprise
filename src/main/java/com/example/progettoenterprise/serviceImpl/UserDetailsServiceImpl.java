package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.security.UtenteLoggato;
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
        Utente utente = utenteRepository.findByUsernameOrEmail(identificativo, identificativo)
                .orElseThrow(() -> new UsernameNotFoundException(messageLang.getMessage("auth.user.notfound", identificativo)));

        // Cambiamo solo il "return new User" con il nostro "UtenteLoggato"
        return new UtenteLoggato(
                utente.getId(), // <--- Passiamo l'ID!
                utente.getUsername(),
                utente.getPassword(),
                List.of(new SimpleGrantedAuthority(utente.getRuolo().name()))
        );
    }
}
