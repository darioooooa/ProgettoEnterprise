package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Notifica;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificaSpecification {

    @Data
    public static class NotificaFilter{
        private Long utenteId;
        private Boolean isLetta;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataMin;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataMax;
        private String parolaChiave;
    }

    public static Specification<Notifica> withFilter(NotificaFilter filter){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtra per utente
            if (filter.getUtenteId() != null) {
                predicates.add(cb.equal(root.get("utente").get("id"), filter.getUtenteId()));
            }

            // Filtra per isLetta
            if (filter.getIsLetta() != null) {
                predicates.add(cb.equal(root.get("isLetta"), filter.getIsLetta()));
            }

            // Filtra per data
            if (filter.getDataMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataCreazione"), filter.getDataMin()));
            }
            if (filter.getDataMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataCreazione"), filter.getDataMax()));
            }

            // Filtra per parola chiave
            if (filter.getParolaChiave() != null && !filter.getParolaChiave().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("messaggio")), "%" + filter.getParolaChiave().toLowerCase() + "%"));
            }

            query.orderBy(cb.desc(root.get("dataCreazione")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
