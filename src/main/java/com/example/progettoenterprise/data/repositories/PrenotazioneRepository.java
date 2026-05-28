package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Prenotazione.StatoPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione,Long>, JpaSpecificationExecutor<Prenotazione> {
    boolean existsByViaggiatoreIdAndViaggioIdAndStato(Long viaggiatoreId, Long viaggioId, StatoPrenotazione stato);

    @Query("""

            SELECT p FROM Prenotazione p
    JOIN FETCH p.viaggio v
    JOIN FETCH p.viaggiatore u
    WHERE v.dataInizio BETWEEN :start AND :end
    AND p.stato = :stato
    """)
    List<Prenotazione> findPrenotazioniPerReminder(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("stato") Prenotazione.StatoPrenotazione stato
    );

    @Query("SELECT p FROM Prenotazione p WHERE p.viaggiatore.id = :utenteId " +
            "AND p.viaggio.dataInizio <= :dataFine " +
            "AND p.viaggio.dataFine >= :dataInizio")
    List<Prenotazione> findPrenotazioniSovrapposte(
            @org.springframework.data.repository.query.Param("utenteId") Long utenteId,
            @org.springframework.data.repository.query.Param("dataInizio") java.time.LocalDate dataInizio,
            @org.springframework.data.repository.query.Param("dataFine") java.time.LocalDate dataFine
    );
    }

