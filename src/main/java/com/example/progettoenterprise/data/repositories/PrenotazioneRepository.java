package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Prenotazione.StatoPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione,Long>, JpaSpecificationExecutor<Prenotazione> {
    boolean existsByViaggiatoreIdAndViaggioIdAndStato(Long viaggiatoreId, Long viaggioId, StatoPrenotazione stato);
}
