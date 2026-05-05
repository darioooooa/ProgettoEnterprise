package com.example.progettoenterprise.auditor;

import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.security.UtenteLoggato;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public record UserAuditorAware(UtenteRepository utenteRepository) implements AuditorAware<Long> {
    @Override
    @NullMarked
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // Se il principal è il nostro contenitore prendiamo l'ID
        if (principal instanceof UtenteLoggato) {
            return Optional.of(((UtenteLoggato) principal).getId());
        }

        return Optional.empty();
    }
}