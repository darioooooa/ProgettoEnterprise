package com.example.progettoenterprise.data.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lista_viaggio")
@Data
public class ListaViaggio {
    @EmbeddedId
    private ListaViaggioKey id;

    @ManyToOne
    @MapsId("listaId")
    @JoinColumn(name = "lista_id")
    private ListaItinerari lista;

    @ManyToOne
    @MapsId("viaggioId") // Collega questo oggetto al campo 'viaggioId' della chiave
    @JoinColumn(name = "viaggio_id")
    private Viaggio viaggio;

    //Altri campi
}