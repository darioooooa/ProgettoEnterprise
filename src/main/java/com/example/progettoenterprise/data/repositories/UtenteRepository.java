package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente,Long>, JpaSpecificationExecutor<Utente> {
    Optional<Utente> findByEmail(String email);
    Optional<Utente> findByUsername(String username);
    Optional<Utente> findByUsernameOrEmail(String username, String email);
}
