package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "amicizia", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"richiedente_id", "ricevente_id"})
})
//UniqueConstraint viene usato per evitare duplicati (richiedente che manda l'amicizia piu volte allo stesso ricevente)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amicizia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "richiedente_id", nullable = false)
    private Utente richiedente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ricevente_id", nullable = false)
    private Utente ricevente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoAmicizia stato;

    private LocalDateTime dataRichiesta;
    private LocalDateTime dataRisposta;

    public enum StatoAmicizia {
        IN_ATTESA,
        ACCETTATA,
        RIFIUTATA
    }
}