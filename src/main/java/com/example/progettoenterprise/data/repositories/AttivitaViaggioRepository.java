package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttivitaViaggioRepository extends JpaRepository<AttivitaViaggio, Long> {
    List<AttivitaViaggio> findByTitolo(String titolo);
    List<AttivitaViaggio>findByDescrizioneContainingIgnoreCase(String descrizione);
    List<AttivitaViaggio>findBycosto(Double costo);
    List<AttivitaViaggio> findByTitoloContainingIgnoreCase(String parteDelTitolo);
    List<AttivitaViaggio>findByoraInizio(String orarioInizio);
    List<AttivitaViaggio>findByoraFine(String orarioFine);

}
