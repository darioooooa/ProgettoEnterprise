package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.ListaItinerari;
import com.example.progettoenterprise.data.entities.ListaItinerari.Visibilita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ListaItinerariRepository extends JpaRepository<ListaItinerari,Long> {
    List<ListaItinerari> findByVisibilita(Visibilita visibilita);
    List<ListaItinerari> findByNomeContainingIgnoreCase(String nome);

    //Le liste di itinerari in cui, tra gli utenti autorizzati, compare un utente che ha uno specifico ID.
    List<ListaItinerari> findByUtentiAutorizzati_Utente_Id(Long utenteId);

    //Trova le liste create in base alla loro visibilità (es. tutte le CONDIVISE)
    List<ListaItinerari> findByVisibilitaOrderByDataCreazioneDesc(Visibilita visibilita);

}
