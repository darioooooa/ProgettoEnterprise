package com.example.progettoenterprise.data.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "lista_itinerari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarioPreferito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Utente proprietario;


    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaViaggio> contenuti;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaUtente> utentiAutorizzati;

}