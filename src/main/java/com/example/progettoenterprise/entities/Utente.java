package com.example.progettoenterprise.entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "utente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;

    private String nome;
    private String cognome;
    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;

    public enum Ruolo {
        VIAGGIATORE,
        ORGANIZZATORE
    }
    //"organizzatore" nel mappedBy ha lo stesso nome della variabile che si trova nella classe Viaggio
    @OneToMany(mappedBy = "organizzatore",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Viaggio> viaggiCreati;  //relazione 1 a molti tra utente e viaggio

    @OneToMany(mappedBy = "viaggiatore",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prenotazione> miePrenotazioni; //relazione 1 a molti tra utente e prenotazione

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaUtente> listeAccessibili;



}
