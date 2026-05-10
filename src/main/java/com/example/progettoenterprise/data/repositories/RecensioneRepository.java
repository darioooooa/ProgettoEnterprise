package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Recensione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecensioneRepository extends JpaRepository<Recensione, Long>, JpaSpecificationExecutor<Recensione> {

    boolean existsByViaggioIdAndUtenteId(Long viaggioId, Long utenteId);

}
