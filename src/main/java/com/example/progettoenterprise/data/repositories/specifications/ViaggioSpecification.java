package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Viaggio;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ViaggioSpecification {

    @Data
    public static class ViaggioFilter {
        private String destinazione;
        private Double prezzoMin;
        private Double prezzoMax;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataInizioMin;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataInizioMax;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataFineMin;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataFineMax;
        private Double mediaRecensioniMin;
        private Integer numRecensioniMin;
        private Long organizzatoreId;
    }

    public static Specification<Viaggio> withFilter(ViaggioFilter viaggioFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtra per destinazione del viaggio
            if (viaggioFilter.getDestinazione() != null && !viaggioFilter.getDestinazione().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("destinazione")), "%" + viaggioFilter.getDestinazione().toLowerCase() + "%"));
            }

            // Filtra per prezzo
            if (viaggioFilter.getPrezzoMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("prezzo"), viaggioFilter.getPrezzoMin()));
            }
            if (viaggioFilter.getPrezzoMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("prezzo"), viaggioFilter.getPrezzoMax()));
            }

            // Filtra per data
            if (viaggioFilter.getDataInizioMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataInizio"), viaggioFilter.getDataInizioMin()));
            }
            if (viaggioFilter.getDataInizioMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataInizio"), viaggioFilter.getDataInizioMax()));
            }
            if (viaggioFilter.getDataFineMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataFine"), viaggioFilter.getDataFineMin()));
            }
            if (viaggioFilter.getDataFineMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataFine"), viaggioFilter.getDataFineMax()));
            }

            // Filtra per recensioni
            if (viaggioFilter.getMediaRecensioniMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("mediaRecensioni"), viaggioFilter.getMediaRecensioniMin()));
            }
            if (viaggioFilter.getNumRecensioniMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numeroRecensioni"), viaggioFilter.getNumRecensioniMin()));
            }

            // Filtra per organizzatore
            if (viaggioFilter.getOrganizzatoreId() != null) {
                predicates.add(cb.equal(root.get("organizzatore").get("id"), viaggioFilter.getOrganizzatoreId()));
            }

            // Ordinamento per data di inizio più recente
            query.orderBy(cb.asc(root.get("dataInizio")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
