package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Prenotazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dataPrenotazione;
    private Integer numeroPersone;

    @Enumerated(EnumType.STRING)
    private StatoPrenotazione stato;

    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente viaggiatore;

    @ManyToOne
    @JoinColumn(name = "viaggio_id")
    private Viaggio viaggio;



    public enum StatoPrenotazione {
        IN_ATTESA, CONFERMATA, ANNULLATA
    }
}