package com.example.progettoenterprise.data.repositories.specifications;

import com.example.progettoenterprise.data.entities.Utente;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UtenteSpecification {

    @Data
    public static class UtenteFilter {
        private String username;
        private String email;
        private String nome;
        private String cognome;
        private Utente.Ruolo ruolo;
    }

    public static Specification<Utente> withFilter(UtenteFilter utenteFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ricerca per username
            if (utenteFilter.getUsername() != null && !utenteFilter.getUsername().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + utenteFilter.getUsername().toLowerCase() + "%"));
            }

            // Ricerca per email
            if (utenteFilter.getEmail() != null && !utenteFilter.getEmail().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + utenteFilter.getEmail().toLowerCase() + "%"));
            }

            // Ricerca per nome
            if (utenteFilter.getNome() != null && !utenteFilter.getNome().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nome")), "%" + utenteFilter.getNome().toLowerCase() + "%"));
            }

            // Ricerca per cognome
            if (utenteFilter.getCognome() != null && !utenteFilter.getCognome().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("cognome")), "%" + utenteFilter.getCognome().toLowerCase() + "%"));
            }

            // Ricerca per ruolo
            if (utenteFilter.getRuolo() != null) {
                predicates.add(cb.equal(root.get("ruolo"), utenteFilter.getRuolo()));
            }

            // Ordina per username
            query.orderBy(cb.asc(root.get("username")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
