package com.example.progettoenterprise.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UtenteLoggato extends User {
    private final Long id;

    public UtenteLoggato(Long id, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "PASSWORD_KEYCLOAK", authorities);
        this.id = id;
    }

}

