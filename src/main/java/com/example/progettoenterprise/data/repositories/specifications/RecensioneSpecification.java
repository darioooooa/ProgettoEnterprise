package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Recensione;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecensioneSpecification {

    @Data
    public static class RecensioneFilter {
        private Long viaggioId;
        private Long utenteId;
        private Integer votoMin;
        private Integer votoMax;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataDa;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataA;
        private String parolaChiave;
    }

    public static Specification<Recensione> withFilter(RecensioneFilter recensioneFilter){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtra per viaggio
            if(recensioneFilter.getViaggioId() != null){
                predicates.add(cb.equal(root.get("viaggio").get("id"), recensioneFilter.getViaggioId()));
            }

            // Filtra per l'utente autore della recensione
            if(recensioneFilter.getUtenteId() != null){
                predicates.add(cb.equal(root.get("utente").get("id"), recensioneFilter.getUtenteId()));
            }

            // Filtra per voto
            if(recensioneFilter.getVotoMin() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("voto"), recensioneFilter.getVotoMin()));
            }
            if(recensioneFilter.getVotoMax() != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("voto"), recensioneFilter.getVotoMax()));
            }

            // Filtra per data
            if(recensioneFilter.getDataDa() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataCreazione"), recensioneFilter.getDataDa()));
            }
            if(recensioneFilter.getDataA() != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("dataCreazione"), recensioneFilter.getDataA()));
            }

            // Filtra per parola chiave
            if(recensioneFilter.getParolaChiave() != null && !recensioneFilter.getParolaChiave().isEmpty()){
                predicates.add(cb.like(cb.lower(root.get("commento")), "%" + recensioneFilter.getParolaChiave().toLowerCase() + "%"));
            }

            // Ordina per la più recente
            query.orderBy(cb.desc(root.get("dataCreazione")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
