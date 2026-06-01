package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "chat_room", uniqueConstraints = {

        @UniqueConstraint(columnNames = {"viaggio_id", "viaggiatore_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggio_id", nullable = false)
    private Viaggio viaggio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggiatore_id", nullable = false)
    private Utente viaggiatore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizzatore_id", nullable = false)
    private Utente organizzatore;

    //  se cancelliamo la stanza, si cancellano i messaggi (CascadeType.ALL)
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessaggioChat> messaggi;
}