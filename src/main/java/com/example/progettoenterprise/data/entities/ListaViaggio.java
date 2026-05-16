package com.example.progettoenterprise.data.entities;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

@Entity
@Table(name = "lista_viaggio")
@Getter
@Setter
public class ListaViaggio {
    @EmbeddedId
    private ListaViaggioKey id=new ListaViaggioKey();

    @ManyToOne
    @MapsId("listaId")
    @JoinColumn(name = "lista_id")
    private ItinerarioPreferito lista;

    @ManyToOne
    @MapsId("viaggioId") // Collega questo oggetto al campo 'viaggioId' della chiave
    @JoinColumn(name = "viaggio_id")
    private Viaggio viaggio;

    //Altri campi
}