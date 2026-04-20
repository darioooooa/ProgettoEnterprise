package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Utente.Ruolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente,Long> {
    Optional<Utente> findByEmail(String email);
    Optional<Utente> findByUsername(String username);
    Optional<Utente> findByUsernameOrEmail(String username, String email);

    List<Utente> findByRuolo(Ruolo ruolo);
    List<Utente> findByUsernameContainingIgnoreCase(String username);
}
