package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RichiestaPromozioneRepository extends JpaRepository<RichiestaPromozione, Long> {
    boolean existsByUsernameRichiestoAndStato(String username, RichiestaPromozione.StatoRichiesta stato);
    boolean existsByEmailProfessionaleAndStato(String email, RichiestaPromozione.StatoRichiesta stato);
}
