package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Segnalazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegnalazioneRepository extends JpaRepository<Segnalazione, Long>, JpaSpecificationExecutor<Segnalazione> {
    long countByStato(Segnalazione.StatoSegnalazione stato);
    boolean existsBySegnalatoreIdAndTipoAndIdRiferimentoAndStatoIn(
            Long segnalatoreId,
            Segnalazione.TipoEntita tipo,
            Long idRiferimento,
            List<Segnalazione.StatoSegnalazione> stati
    );
}
