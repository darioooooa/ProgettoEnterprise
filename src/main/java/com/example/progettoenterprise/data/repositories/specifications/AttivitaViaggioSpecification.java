package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttivitaViaggioSpecification {

    @Data
    public static class AttivitaFilter {
        private Long viaggioId;
        private String titolo;
        private String posizione;
        private Double costoMin;
        private Double costoMax;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime orarioInizioMin;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime orarioInizioMax;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime orarioFineMin;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime orarioFineMax;
    }

    public static Specification<AttivitaViaggio> withFilter(AttivitaFilter attivitaFilter){
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Filtra sul viaggio
            if (attivitaFilter.getViaggioId() != null) {
                predicates.add(cb.equal(root.get("viaggio").get("id"), attivitaFilter.getViaggioId()));
            }

            // Filtra sul titolo
            if (attivitaFilter.getTitolo() != null && !attivitaFilter.getTitolo().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("titolo")), "%" + attivitaFilter.getTitolo().toLowerCase() + "%"));
            }

            // Filtra per posizione
            if (attivitaFilter.getPosizione() != null && !attivitaFilter.getPosizione().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("posizione")), "%" + attivitaFilter.getPosizione().toLowerCase() + "%"));
            }

            // Filtra per costo
            if(attivitaFilter.getCostoMin() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("costo"), attivitaFilter.getCostoMin()));
            }
            if (attivitaFilter.getCostoMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("costo"), attivitaFilter.getCostoMax()));
            }

            // Filtra per date inizio e fine
            if(attivitaFilter.getOrarioInizioMin() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("orarioInizio"), attivitaFilter.getOrarioInizioMin()));
            }
            if (attivitaFilter.getOrarioInizioMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orarioInizio"), attivitaFilter.getOrarioInizioMax()));
            }
            if(attivitaFilter.getOrarioFineMin() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("orarioFine"), attivitaFilter.getOrarioFineMin()));
            }
            if (attivitaFilter.getOrarioFineMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orarioFine"), attivitaFilter.getOrarioFineMax()));
            }

            // Ordina per orario di inizio
            query.orderBy(cb.asc(root.get("orarioInizio")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
