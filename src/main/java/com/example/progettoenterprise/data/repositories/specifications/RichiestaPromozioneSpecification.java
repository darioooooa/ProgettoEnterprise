package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.data.entities.RichiestaPromozione.StatoRichiesta;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RichiestaPromozioneSpecification {

    @Data
    public static class RichiestaFilter {
        private StatoRichiesta stato;
        private Long viaggiatoreId;
        private Long adminId;
    }

    public static Specification<RichiestaPromozione> withFilter(RichiestaFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtra per stato
            if (filter.getStato() != null) {
                predicates.add(cb.equal(root.get("stato"), filter.getStato()));
            }

            // Filtra per utente che ha fatto la richiesta
            if (filter.getViaggiatoreId() != null) {
                predicates.add(cb.equal(root.get("viaggiatore").get("id"), filter.getViaggiatoreId()));
            }

            // Filtra per amministratore che ha valutato la richiesta
            if (filter.getAdminId() != null) {
                predicates.add(cb.equal(root.get("adminId"), filter.getAdminId()));
            }

            // Ordina dalla richiesta più vecchia alla più nuova
            query.orderBy(cb.asc(root.get("dataRichiesta")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
