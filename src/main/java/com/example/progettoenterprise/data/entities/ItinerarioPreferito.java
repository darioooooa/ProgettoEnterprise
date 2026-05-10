package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "lista_itinerari")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ItinerarioPreferito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @CreatedDate
    @Column(name = "data_creazione", updatable = false)
    private LocalDate dataCreazione;

    @LastModifiedDate
    @Column(name = "ultima_modifica")
    private LocalDateTime ultimaModifica;

    @CreatedBy
    @Column(name = "creato_da", updatable = false)
    private Long creatoDa;

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