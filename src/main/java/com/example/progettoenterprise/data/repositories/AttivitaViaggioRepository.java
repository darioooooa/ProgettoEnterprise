package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttivitaViaggioRepository extends JpaRepository<AttivitaViaggio, Long> {
    List<AttivitaViaggio> findByTitolo(String titolo);
    List<AttivitaViaggio> findByDescrizioneContainingIgnoreCase(String descrizione);
    List<AttivitaViaggio> findByCosto(Double costo);
    List<AttivitaViaggio> findByTitoloContainingIgnoreCase(String parteDelTitolo);
    List<AttivitaViaggio> findByOrarioInizio(java.time.LocalDateTime orarioInizio);
    List<AttivitaViaggio> findByOrarioFine(java.time.LocalDateTime orarioFine);

}
