package com.example.progettoenterprise.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "lista_itinerari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaItinerari {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_itinerario;

    @Column(nullable = false)
    private String nome;

    private LocalDate dataCreazione;

    @Enumerated(EnumType.STRING)
    private Visibilita visibilita;

    public enum Visibilita {
        PRIVATA,
        PUBBLICA,
        CONDIVISA
    }

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaViaggio> contenuti;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaUtente> utentiAutorizzati;

}
