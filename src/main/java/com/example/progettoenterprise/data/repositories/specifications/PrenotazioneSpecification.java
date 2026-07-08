package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Utente;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneSpecification {

    @Data
    public static class PrenotazioneFilter {
        private Long viaggioId;
        private Long viaggiatoreId;
        private Long organizzatoreProprietarioId;
        private Prenotazione.StatoPrenotazione stato;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataPrenotazioneMin;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime dataPrenotazioneMax;
        private Integer numeroPersoneMin;
        private Integer numeroPersoneMax;
        private String usernameViaggiatore;
    }

    public static Specification<Prenotazione> withFilter(PrenotazioneFilter prenotazioneFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (prenotazioneFilter.getViaggioId() != null) {
                predicates.add(cb.equal(root.get("viaggio").get("id"), prenotazioneFilter.getViaggioId()));
            }

            if (prenotazioneFilter.getStato() != null) {
                predicates.add(cb.equal(root.get("stato"), prenotazioneFilter.getStato()));
            } else if (prenotazioneFilter.getOrganizzatoreProprietarioId() != null) {
                predicates.add(cb.notEqual(root.get("stato"), Prenotazione.StatoPrenotazione.IN_ATTESA));
            }

            if (prenotazioneFilter.getViaggiatoreId() != null) {
                predicates.add(cb.equal(root.get("viaggiatore").get("id"), prenotazioneFilter.getViaggiatoreId()));
            }

            if (prenotazioneFilter.getDataPrenotazioneMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataPrenotazione"), prenotazioneFilter.getDataPrenotazioneMin()));
            }
            if (prenotazioneFilter.getDataPrenotazioneMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataPrenotazione"), prenotazioneFilter.getDataPrenotazioneMax()));
            }

            if (prenotazioneFilter.getNumeroPersoneMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numeroPersone"), prenotazioneFilter.getNumeroPersoneMin()));
            }
            if (prenotazioneFilter.getNumeroPersoneMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("numeroPersone"), prenotazioneFilter.getNumeroPersoneMax()));
            }

            if (prenotazioneFilter.getOrganizzatoreProprietarioId() != null) {
                predicates.add(cb.equal(root.get("viaggio").get("organizzatore").get("id"), prenotazioneFilter.getOrganizzatoreProprietarioId()));
            }

            // Filtro per username viaggiatore (ricerca parziale case-insensitive)
            if (prenotazioneFilter.getUsernameViaggiatore() != null && !prenotazioneFilter.getUsernameViaggiatore().isBlank()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var subRoot = subquery.from(Utente.class);
                subquery.select(subRoot.get("id"));
                subquery.where(cb.like(
                        cb.lower(subRoot.get("username")),
                        "%" + prenotazioneFilter.getUsernameViaggiatore().toLowerCase() + "%"
                ));
                predicates.add(root.get("viaggiatore").get("id").in(subquery));
            }

            query.orderBy(cb.desc(root.get("dataPrenotazione")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}