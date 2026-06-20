package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String titolareCarta;

    private String circuito;

    private BigDecimal importo;

    private String ricevutaPagamento;//token di stripe per il rimborso

    public enum StatoPagamento {
        COMPLETATO,
        IN_ATTESA,
        RIMBORSATO,
        RIMBORSO_FALLITO
    }

    @Enumerated(EnumType.STRING)
    private StatoPagamento statoPagamento;

    @OneToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "prenotazione_id")
    private Prenotazione prenotazione;

    @CreatedBy
    @Column(name = "creato_da", updatable = false)
    private Long creatoDa;

    @CreatedDate
    @Column(name = "data_inserimento", updatable = false)
    private LocalDateTime dataInserimento;

    @LastModifiedDate
    @Column(name = "ultima_modifica")
    private LocalDateTime ultimaModifica;


}
