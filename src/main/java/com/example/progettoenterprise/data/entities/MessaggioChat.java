package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messaggio_chat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessaggioChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;


    @Column(name = "mittente_username", nullable = false)
    private String mittenteUsername;

    @Column(nullable = false, length = 2000)
    private String testo;

    @Column(name = "data_invio", nullable = false)
    private LocalDateTime dataInvio;

    @Column(nullable = false)
    private boolean letto=false;
}