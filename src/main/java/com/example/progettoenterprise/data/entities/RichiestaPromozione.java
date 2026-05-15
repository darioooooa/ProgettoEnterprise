package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "richiesta_promozione")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaPromozione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggiatore_id", nullable = false)
    private Viaggiatore viaggiatore;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatoRichiesta stato = StatoRichiesta.IN_ATTESA;

    @Column(length = 1000)
    private String motivazione;

    @Column(length = 500)
    private String documentiLink;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime dataRichiesta;

    @LastModifiedDate
    private LocalDateTime dataValutazione;

    @Column(columnDefinition = "TEXT")
    private String biografiaProfessionale;

    @Column(name = "admin_id")
    @LastModifiedBy
    private Long adminId;

    public enum StatoRichiesta {
        IN_ATTESA,
        APPROVATA,
        RIFIUTATA
    }


}


