package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Organizzatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface OrganizzatoreRepository extends JpaRepository<Organizzatore,Long> {

    Optional<Organizzatore> findByUsername(String username);

    Optional<Organizzatore> findByEmail(String email);
}
