package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Recensione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecensioneRepository extends JpaRepository<Recensione, Long> {

    List<Recensione> findByViaggioId(Long viaggioId);

    boolean existsByViaggioIdAndUtenteId(Long viaggioId, Long utenteId);

}
