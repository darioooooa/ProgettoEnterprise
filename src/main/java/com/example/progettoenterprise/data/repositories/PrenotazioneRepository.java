package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Prenotazione.StatoPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione,Long> {
    List<Prenotazione> findByViaggiatoreId(Long utenteId);
    List<Prenotazione> findByViaggioId(Long viaggioId);
    List<Prenotazione> findByStato(StatoPrenotazione stato);

    List<Prenotazione> findByViaggiatoreIdAndViaggioId(Long viaggiatoreId, Long viaggioId);
    long countByViaggioIdAndStato(Long viaggioId, StatoPrenotazione stato);
}
