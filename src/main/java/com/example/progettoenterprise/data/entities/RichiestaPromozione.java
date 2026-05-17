package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @Column(name = "documenti_link", length = 500)
    private String documentiLink;

    @CreatedDate
    @Column(name = "data_richiesta", updatable = false)
    private LocalDateTime dataRichiesta;

    @LastModifiedDate
    @Column(name = "data_valutazione")
    private LocalDateTime dataValutazione;

    @Column(name = "biografia_professionale", columnDefinition = "TEXT")
    private String biografiaProfessionale;

    @Column(name = "admin_id")
    @LastModifiedBy
    private Long adminId;

    public enum StatoRichiesta {
        IN_ATTESA,
        APPROVATA,
        RIFIUTATA
    }

    @NotBlank
    @Column(name = "username_richiesto", unique = true, nullable = false)
    private String usernameRichiesto;

    @NotBlank
    @Email
    @Column(name = "email_professionale", unique = true, nullable = false)
    private String emailProfessionale;


}


