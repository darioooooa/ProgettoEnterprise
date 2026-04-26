package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.Amicizia;
import com.example.progettoenterprise.data.entities.Amicizia.StatoAmicizia;
import com.example.progettoenterprise.data.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmiciziaRepository extends JpaRepository<Amicizia, Long> {
    Optional<Amicizia> findByRichiedenteAndRicevente(Utente richiedente, Utente ricevente);
    List<Amicizia> findByRiceventeAndStato(Utente ricevente, StatoAmicizia stato);
    List<Amicizia> findByRichiedenteAndStato(Utente richiedente, StatoAmicizia stato);

    // Serve per evitare che entrambi si mandino la richiesta contemporaneamente
    @Query("SELECT a FROM Amicizia a WHERE " +
            "(a.richiedente = :u1 AND a.ricevente = :u2) OR " +
            "(a.richiedente = :u2 AND a.ricevente = :u1)")
    Optional<Amicizia> findQualsiasiRelazione(@Param("u1") Utente u1, @Param("u2") Utente u2);

    // Questa query cerca tutte le amicizie ACCETTATE dove l'utente è o chi ha chiesto o chi ha ricevuto
    @Query("SELECT a FROM Amicizia a WHERE " +
            "a.stato = 'ACCETTATA' AND (a.richiedente = :utente OR a.ricevente = :utente)")
    List<Amicizia> findAllAmiciConfermati(@Param("utente") Utente utente);
}
