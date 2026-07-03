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
        private String usernameViaggiatore;
    }

    public static Specification<RichiestaPromozione> withFilter(RichiestaFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStato() != null) {
                predicates.add(cb.equal(root.get("stato"), filter.getStato()));
            }

            if (filter.getViaggiatoreId() != null) {
                predicates.add(cb.equal(root.get("viaggiatore").get("id"), filter.getViaggiatoreId()));
            }

            if (filter.getAdminId() != null) {
                predicates.add(cb.equal(root.get("adminId"), filter.getAdminId()));
            }

            //filtro per username del viaggiatore
            if (filter.getUsernameViaggiatore() != null && !filter.getUsernameViaggiatore().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("viaggiatore").get("username")),
                        "%" + filter.getUsernameViaggiatore().toLowerCase() + "%"
                ));
            }

            query.orderBy(cb.desc(root.get("dataRichiesta")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}