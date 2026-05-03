package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.ImmagineViaggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImmagineViaggioRepository extends JpaRepository<ImmagineViaggio, Long> {
    // Trova tutte le immagini di un viaggio specifico
    List<ImmagineViaggio> findByViaggioId(Long viaggioId);

    // Trova solo le immagini pubbliche di un viaggio
    List<ImmagineViaggio> findByViaggioIdAndPubblicaTrue(Long viaggioId);

    // Restituisce il numero di immagini di un viaggio
    long countByViaggioId(Long viaggioId);

}
