package com.example.progettoenterprise.data.repositories.specifications;
import com.example.progettoenterprise.data.entities.Segnalazione;
import com.example.progettoenterprise.data.entities.Segnalazione.TipoEntita;
import com.example.progettoenterprise.data.entities.Segnalazione.StatoSegnalazione;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SegnalazioneSpecification {
    @Data
    public static class SegnalazioneFilter{
        private TipoEntita tipo;
        private StatoSegnalazione stato;
        private Long segnalatoreId;
        private Long adminId;
    }

    public static Specification<Segnalazione> withFilter(SegnalazioneSpecification.SegnalazioneFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Filtre per tipo
            if (filter.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filter.getTipo()));
            }
            // Filtra per stato
            if (filter.getStato() != null) {
                predicates.add(cb.equal(root.get("stato"), filter.getStato()));
            }

            // Filtra per utente che ha effettuato la segnalazione
            if (filter.getSegnalatoreId() != null) {
                predicates.add(cb.equal(root.get("segnalatoreId"), filter.getSegnalatoreId()));
            }

            // Filtra  per amministratore che ha preso in carico la segnalazione
            if (filter.getAdminId() != null) {
                predicates.add(cb.equal(root.get("adminId"), filter.getAdminId()));
            }
            query.orderBy(cb.asc(root.get("dataSegnalazione")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
