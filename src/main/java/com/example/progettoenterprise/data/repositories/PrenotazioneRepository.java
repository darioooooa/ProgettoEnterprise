package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Prenotazione.StatoPrenotazione;
import com.example.progettoenterprise.data.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
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

    List<Prenotazione> findByViaggioIdAndStato(Long viaggioId, Prenotazione.StatoPrenotazione stato);

    Optional<Prenotazione> findByViaggioIdAndViaggiatoreId(Long viaggioId, Long viaggiatoreId);

    @Query("SELECT DISTINCT p.viaggiatore FROM Prenotazione p " +
            "WHERE LOWER(p.viaggio.destinazione) = LOWER(:destinazione) " +
            "AND p.viaggio.dataFine < CURRENT_DATE " +
            "AND p.stato = 'CONFERMATA'")
    List<Utente> findViaggiatoriViaggiGiaFatti(@Param("destinazione") String destinazione);
}

