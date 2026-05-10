package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
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

    @CreatedDate
    @Column(name = "data_prenotazione", updatable = false)
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


    @CreatedBy
    @Column(name = "creato_da", updatable = false)
    private Long creatoDa;

    @LastModifiedDate
    @Column(name = "data_ultima_modifica")
    private LocalDateTime dataUltimaModifica;

    @LastModifiedBy
    @Column(name = "modificato_da")
    private Long modificatoDa;

    public enum StatoPrenotazione {
        IN_ATTESA, CONFERMATA, ANNULLATA
    }
}