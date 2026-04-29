package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.Itinerario;
import com.example.progettoenterprise.data.entities.Itinerario.Visibilita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario,Long> {
    List<Itinerario> findByVisibilita(Visibilita visibilita);
    List<Itinerario> findByNomeContainingIgnoreCase(String nome);

    //Le liste di itinerari in cui, tra gli utenti autorizzati, compare un utente che ha uno specifico ID.
    List<Itinerario> findByUtentiAutorizzati_Utente_Id(Long utenteId);

    //Trova le liste create in base alla loro visibilità (es. tutte le CONDIVISE)
    List<Itinerario> findByVisibilitaOrderByDataCreazioneDesc(Visibilita visibilita);

}
