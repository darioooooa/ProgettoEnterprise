package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Segnalazione;
import com.example.progettoenterprise.data.entities.Segnalazione.TipoEntita;
import com.example.progettoenterprise.data.entities.Segnalazione.StatoSegnalazione;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SegnalazioneSpecification {

    @Data
    public static class SegnalazioneFilter {
        private TipoEntita tipo;
        private List<StatoSegnalazione> stato;
        private Long segnalatoreId;
        private Long adminId;
        private String usernameSegnalatore;
    }

    public static Specification<Segnalazione> withFilter(SegnalazioneSpecification.SegnalazioneFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filter.getTipo()));
            }

            if (filter.getStato() != null && !filter.getStato().isEmpty()) {
                predicates.add(root.get("stato").in(filter.getStato()));
            }

            if (filter.getSegnalatoreId() != null) {
                predicates.add(cb.equal(root.get("segnalatoreId"), filter.getSegnalatoreId()));
            }

            if (filter.getAdminId() != null) {
                predicates.add(cb.equal(root.get("adminId"), filter.getAdminId()));
            }

            if (filter.getUsernameSegnalatore() != null && !filter.getUsernameSegnalatore().isBlank()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var subRoot = subquery.from(com.example.progettoenterprise.data.entities.Utente.class);
                subquery.select(subRoot.get("id"));
                subquery.where(cb.like(
                        cb.lower(subRoot.get("username")),
                        "%" + filter.getUsernameSegnalatore().toLowerCase() + "%"
                ));
                predicates.add(root.get("segnalatoreId").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}