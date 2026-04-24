package com.example.progettoenterprise.data.repositories;
import com.example.progettoenterprise.data.entities.Viaggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViaggioRepository extends JpaRepository<Viaggio,Long> {
    List<Viaggio> findViaggioByDestinazione(Long destinazione);
    List<Viaggio> findViaggioByPrezzo(Long prezzo);
    List<Viaggio> findViaggioByDataInizio(Date dataInizio);
    List<Viaggio> findViaggioByDataFine(Date dataFine);
    List<Viaggio> findViaggioByTitolo(String titolo);
    List<Viaggio> findViaggioByOrganizzatoreId(Long organizzatore_id);

    // Metodi per le recensioni

    // Aggiunta di una nuova recensione al viaggio
    // Formula: mediaRecensioni = (mediaRecensioni * numeroRecensioni + nuovoVoto) / (numeroRecensioni + 1)
    @Modifying
    @Query("UPDATE Viaggio v SET " +
            "v.mediaRecensioni = (v.mediaRecensioni * v.numeroRecensioni + :voto) / (v.numeroRecensioni + 1), " +
            "v.numeroRecensioni = v.numeroRecensioni + 1 " +
            "WHERE v.id = :viaggioId")
    void aggiornaStatisticheRecensione(@Param("viaggioId") Long viaggioId,@Param("voto") int voto);

    // Modifica di una recensione esistente
    // Formula: mediaRecensioni = (mediaRecensioni * numeroRecensioni - votoVecchio + votoNuovo) / numeroRecensioni
    @Modifying
    @Query("UPDATE Viaggio v SET " +
            "v.mediaRecensioni = CASE WHEN v.numeroRecensioni > 1 " +
            "THEN (v.mediaRecensioni * v.numeroRecensioni - :votoVecchio + :votoNuovo) / v.numeroRecensioni " +
            "ELSE :votoNuovo END " +
            "WHERE v.id = :viaggioId")
    void ricalcolaMediaPerModifica(@Param("viaggioId") Long viaggioId, @Param("votoVecchio") int votoVecchio, @Param("votoNuovo") int votoNuovo);

    // Eliminazione di una recensione
    // Formula: mediaRecensioni = (mediaRecensioni * numeroRecensioni - votoSottratto) / (numeroRecensioni - 1)
    @Modifying
    @Query("UPDATE Viaggio v SET " +
            "v.mediaRecensioni = CASE WHEN v.numeroRecensioni > 1 " +
            "THEN (v.mediaRecensioni * v.numeroRecensioni - :votoSottratto) / (v.numeroRecensioni - 1) " +
            "ELSE 0.0 END, " +
            "v.numeroRecensioni = v.numeroRecensioni - 1 " +
            "WHERE v.id = :viaggioId")
    void ricalcolaMediaPerEliminazione(@Param("viaggioId") Long viaggioId, @Param("votoSottratto") int votoSottratto);
}
