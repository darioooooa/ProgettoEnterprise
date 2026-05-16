package com.example.progettoenterprise.data.entities;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "viaggio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Viaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titolo;

    @Column(length = 1000)
    private String descrizione;
    @CreatedBy
    @Column(name = "creato_da", updatable = false)
    private Long creatoDa;

    private String destinazione;

    private Double prezzo;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;

    private Double latitudine;
    private Double longitudine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizzatore_id") //il nome del campo che è stato aggiunto nel nostro db
    private Utente organizzatore;

    @OneToMany(mappedBy = "viaggio",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prenotazione> prenotazioniRicevute; //relazione 1 a molti tra viaggio e prenotazione

    @OneToMany(mappedBy = "viaggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaViaggio> presenteInListe;

    @OneToMany(mappedBy = "viaggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttivitaViaggio> tappe;


    @Column(name="media_recensioni", nullable = false)
    private Double mediaRecensioni=0.0;
    @Column(name = "numero_recensioni", nullable = false)
    private int numeroRecensioni=0;

    @OneToMany(mappedBy = "viaggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recensione> recensioni;

    @OneToMany(mappedBy = "viaggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImmagineViaggio> galleria;
}