package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RichiestaPromozioneRepository extends JpaRepository<RichiestaPromozione, Long> {
    boolean existsByUsernameRichiestoAndStato(String username, RichiestaPromozione.StatoRichiesta stato);
    boolean existsByEmailProfessionaleAndStato(String email, RichiestaPromozione.StatoRichiesta stato);
    boolean existsByViaggiatoreIdAndStato(Long viaggiatoreId, RichiestaPromozione.StatoRichiesta stato);
    Optional<RichiestaPromozione> findByViaggiatoreIdAndStato(Long viaggiatoreId, RichiestaPromozione.StatoRichiesta stato);
    boolean existsByUsernameRichiesto(String usernameRichiesto);
    boolean existsByEmailProfessionale(String emailProfessionale);
    boolean existsByUsernameRichiestoAndStatoNot(String usernameRichiesto, RichiestaPromozione.StatoRichiesta stato);
    boolean existsByEmailProfessionaleAndStatoNot(String emailProfessionale, RichiestaPromozione.StatoRichiesta stato);
    Optional<RichiestaPromozione> findFirstByViaggiatoreIdAndStatoOrderByDataRichiestaDesc(Long viaggiatoreId, RichiestaPromozione.StatoRichiesta stato);
}
