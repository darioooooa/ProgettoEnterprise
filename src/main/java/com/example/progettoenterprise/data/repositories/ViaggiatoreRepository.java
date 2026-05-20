package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Viaggiatore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViaggiatoreRepository extends JpaRepository<Viaggiatore, Long> {
    List<Viaggiatore> findByUsernameContainingIgnoreCase(String nomeUtente);// utile per la barra di ricerca

}
