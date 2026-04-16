package com.example.progettoenterprise.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "viaggio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Viaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_viaggio;

    @Column(nullable = false)
    private String titolo;

    @Column(length = 1000)
    private String descrizione;

    private String destinazione;

    private Double prezzo;
    private LocalDate dataInizio;
    private LocalDate dataFine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizzatore_id") //il nome del campo che è stato aggiunto nel nostro db
    private Utente organizzatore;

    @OneToMany(mappedBy = "viaggio",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prenotazione> prenotazioniRicevute; //relazione 1 a molti tra viaggio e prenotazione

    @OneToMany(mappedBy = "viaggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaViaggio> presenteInListe;
}
