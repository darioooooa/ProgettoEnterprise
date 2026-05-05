package com.example.progettoenterprise.config.auditor;

import com.example.progettoenterprise.data.repositories.UtenteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditorConfig {
    private final UtenteRepository utenteRepository;

    public AuditorConfig(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @Bean
    public AuditorAware<Long> auditorProvider() {

        return new UserAuditorAware(utenteRepository);
    }
}