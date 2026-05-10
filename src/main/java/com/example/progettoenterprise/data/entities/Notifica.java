package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notifica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messaggio;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime dataCreazione;

    @CreatedBy
    @Column(updatable = false)
    private Long creatoDa;

    private boolean isLetta=false;

    private Long idRiferimento; //riferimento al viaggio che è stato prenotato/altre cose

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "utente_id")
    private Utente utente;





}
