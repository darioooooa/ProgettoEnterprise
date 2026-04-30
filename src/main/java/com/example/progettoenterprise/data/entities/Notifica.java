package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messaggio;
    private LocalDateTime dataCreazione;

    private boolean isLetta=false;

    private Long idRiferimento; //riferimento al viaggio che è stato prenotato/altre cose

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "utente_id")
    private Utente utente;





}
