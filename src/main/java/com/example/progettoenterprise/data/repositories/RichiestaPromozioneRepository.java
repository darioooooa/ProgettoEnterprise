package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RichiestaPromozioneRepository extends JpaRepository<RichiestaPromozione, Long> {
    List<RichiestaPromozione> findByStato(RichiestaPromozione.StatoRichiesta stato);
}
