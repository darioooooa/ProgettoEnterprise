package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "amicizia", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"richiedente_id", "ricevente_id"})
})
//UniqueConstraint viene usato per evitare duplicati (richiedente che manda l'amicizia piu volte allo stesso ricevente)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime dataRichiesta;

    @LastModifiedDate
    private LocalDateTime dataRisposta;
    public enum StatoAmicizia {
        IN_ATTESA,
        ACCETTATA,
        RIFIUTATA
    }
}