package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.data.entities.Utente;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente,Long>, JpaSpecificationExecutor<Utente> {

    @Cacheable(value = CacheConfig.CACHE_UTENTI_AUTH, key = "#email", unless = "#result == null")
    Optional<Utente> findByEmail(String email);
    Optional<Utente> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Utente u WHERE u.isAttivo = false")
    List<Utente> findByIsAttivoFalse();
}
