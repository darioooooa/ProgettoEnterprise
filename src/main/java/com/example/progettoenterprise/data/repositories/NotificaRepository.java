package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificaRepository extends JpaRepository<Notifica, Long>, JpaSpecificationExecutor<Notifica> {
    List<Notifica> findAllByUtenteIdAndIsLettaIsFalseOrderByDataCreazioneDesc(Long utenteId);
    long countByUtenteIdAndIsLettaIsFalse(Long utenteId);
}
