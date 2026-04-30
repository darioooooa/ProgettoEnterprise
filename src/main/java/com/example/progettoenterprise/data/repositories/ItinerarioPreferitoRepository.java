package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito.Visibilita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ItinerarioPreferitoRepository extends JpaRepository<ItinerarioPreferito,Long> {
    List<ItinerarioPreferito> findByVisibilita(Visibilita visibilita);
    List<ItinerarioPreferito> findByNomeContainingIgnoreCase(String nome);

    //Le liste di itinerari in cui, tra gli utenti autorizzati, compare un utente che ha uno specifico ID.
    List<ItinerarioPreferito> findByUtentiAutorizzati_Utente_Id(Long utenteId);

    //Trova le liste create in base alla loro visibilità (es. tutte le CONDIVISE)
    List<ItinerarioPreferito> findByVisibilitaOrderByDataCreazioneDesc(Visibilita visibilita);

}