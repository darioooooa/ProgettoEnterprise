package com.example.progettoenterprise.data.repositories;


import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito.Visibilita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ItinerarioPreferitoRepository extends JpaRepository<ItinerarioPreferito, Long> {

    // Trova tutte le liste di un utente specifico
    List<ItinerarioPreferito> findByProprietarioId(Long proprietarioId);

    // Cerca per nome solo tra quelle pubbliche (molto più sicuro ed efficiente)
    List<ItinerarioPreferito> findByNomeContainingIgnoreCaseAndVisibilita(String nome, Visibilita visibilita);

    List<ItinerarioPreferito> findByUtentiAutorizzati_Utente_Id(Long utenteId);

    List<ItinerarioPreferito> findByVisibilitaOrderByDataCreazioneDesc(Visibilita visibilita);
}