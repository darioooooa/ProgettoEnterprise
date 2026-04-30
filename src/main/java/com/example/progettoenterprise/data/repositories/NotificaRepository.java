package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificaRepository extends JpaRepository<Notifica, Long> {
    List<Notifica> findByUtenteId(Long utenteId);
    List<Notifica> findAllByUtenteIdOrderByDataNotificaDesc();
    List<Notifica> findAllByUtenteIdAndIsLettaIsFalseOrderByDataNotificaDesc(Long utenteId);
    long countByUtenteIdAndIsLettaIsFalse(Long utenteId);
}
