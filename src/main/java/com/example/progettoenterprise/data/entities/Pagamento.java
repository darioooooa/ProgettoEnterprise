package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(length = 16)
    private String numeroCarta;

    @Column(length = 5)
    private String dataScadenza;

    @Column(length = 3)
    private String cvv; //

    private String circuito;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggiatore_id")
    private Viaggiatore viaggiatore;

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
