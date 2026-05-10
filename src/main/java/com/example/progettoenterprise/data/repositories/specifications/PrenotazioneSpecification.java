package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Prenotazione;
import jakarta.persistence.criteria.Predicate;
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
    }

    public static Specification<Prenotazione> withFilter(PrenotazioneFilter prenotazioneFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtra per un viaggio specifico
            if (prenotazioneFilter.getViaggioId() != null) {
                predicates.add(cb.equal(root.get("viaggio").get("id"), prenotazioneFilter.getViaggioId()));
            }

            // Filtra per stato
            if (prenotazioneFilter.getStato() != null) {
                predicates.add(cb.equal(root.get("stato"), prenotazioneFilter.getStato()));
            }

            // Filtra per un utente specifico
            if (prenotazioneFilter.getViaggiatoreId() != null) {
                predicates.add(cb.equal(root.get("viaggiatore").get("id"), prenotazioneFilter.getViaggiatoreId()));
            }

            // Filtra per data prenotazione
            if (prenotazioneFilter.getDataPrenotazioneMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataPrenotazione"), prenotazioneFilter.getDataPrenotazioneMin()));
            }
            if (prenotazioneFilter.getDataPrenotazioneMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataPrenotazione"), prenotazioneFilter.getDataPrenotazioneMax()));
            }

            // Filtra per numero persone
            if (prenotazioneFilter.getNumeroPersoneMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numeroPersone"), prenotazioneFilter.getNumeroPersoneMin()));
            }
            if (prenotazioneFilter.getNumeroPersoneMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("numeroPersone"), prenotazioneFilter.getNumeroPersoneMax()));
            }

            // Filtra per organizzatore proprietario
            if (prenotazioneFilter.getOrganizzatoreProprietarioId() != null) {
                predicates.add(cb.equal(root.get("viaggio").get("organizzatore").get("id"), prenotazioneFilter.getOrganizzatoreProprietarioId()));
            }

            // Ordina per data di prenotazione più recente
            query.orderBy(cb.desc(root.get("dataPrenotazione")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
