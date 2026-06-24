package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.ListaUtente;
import com.example.progettoenterprise.data.entities.ListaUtenteKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaUtenteRepository extends JpaRepository<ListaUtente, ListaUtenteKey> {
    List<ListaUtente> findByUtenteIdAndStato(Long utenteId, ListaUtente.StatoInvito stato);
    boolean existsByListaIdAndStato(Long listaId, ListaUtente.StatoInvito stato);
}
