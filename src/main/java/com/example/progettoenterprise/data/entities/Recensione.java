package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recensione")
@AllArgsConstructor
@NoArgsConstructor
public class Recensione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    @NotNull(message = "Il voto è obbligatorio")
    @Column(nullable = false)
    private int voto;

    @Column(length = 500)
    @Size(max = 500, message = "Limite di caratteri del commento superato")
    private String commento;

    @ManyToOne(fetch = FetchType.LAZY)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY)
    private Viaggio viaggio;

    @Column(name = "data_creazione", nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    // Eseguito in automatico alla creazione
    @PrePersist
    protected void onCreate() {
        this.dataCreazione = LocalDateTime.now();
    }
}
