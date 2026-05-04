package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttivitaViaggioRepository extends JpaRepository<AttivitaViaggio, Long> {
    List<AttivitaViaggio> findByViaggioIdAndTitoloContainingIgnoreCase(Long viaggioId, String titolo);
    List<AttivitaViaggio> findByDescrizioneContainingIgnoreCase(String descrizione);
    List<AttivitaViaggio> findByViaggioIdAndCostoLessThanEqual(Long viaggioId, Double costo);
    List<AttivitaViaggio> findByTitoloContainingIgnoreCase(String parteDelTitolo);
    List<AttivitaViaggio> findByViaggioIdOrderByOrarioInizioAsc(Long viaggioId);
    List<AttivitaViaggio> findByViaggioIdAndOrarioFine(Long viaggio_id, LocalDateTime orarioFine);

    Long id(Long id);
}
