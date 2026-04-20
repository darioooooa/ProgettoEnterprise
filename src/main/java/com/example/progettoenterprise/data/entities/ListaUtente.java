package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lista_utente")
@Data
public class ListaUtente {

    @EmbeddedId
    private ListaUtenteKey id = new ListaUtenteKey();

    @ManyToOne
    @MapsId("utenteId") // Collega l'utente alla sua parte della chiave
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne
    @MapsId("listaId") // Collega la lista alla sua parte della chiave
    @JoinColumn(name = "lista_id")
    private ListaItinerari lista;

    //Altri campi
}