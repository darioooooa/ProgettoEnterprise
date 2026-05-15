package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "segnalazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Segnalazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEntita tipo;

    @Column(nullable = false)
    private Long idRiferimento;

    @Column(nullable = false)
    private String motivo;

    @Column(length = 1000)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoSegnalazione stato = StatoSegnalazione.APERTA;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime dataSegnalazione;

    @CreatedBy
    @Column(updatable = false)
    private Long segnalatoreId;

    @LastModifiedDate
    private LocalDateTime dataRisoluzione;

    @LastModifiedBy
    private Long adminId;

    public enum TipoEntita {
        UTENTE,
        VIAGGIO,
        RECENSIONE,
        ITINERARIO
    }

    public enum StatoSegnalazione {
        APERTA,
        IN_LAVORAZIONE,
        CHIUSA,
        RIFIUTATA
    }
}