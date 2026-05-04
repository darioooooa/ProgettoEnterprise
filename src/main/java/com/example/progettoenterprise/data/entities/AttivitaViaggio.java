package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attivita_viaggio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttivitaViaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titolo;

    @Column(length = 500)
    private String descrizione;

    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;

    private String posizione;
    private Double costo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggio_id", nullable = false)
    private Viaggio viaggio;
}