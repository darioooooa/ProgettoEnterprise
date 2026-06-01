package com.example.progettoenterprise.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessaggioChatDTO {
    private Long id;
    private Long chatRoomId;
    private String mittenteUsername;
    private String testo;
    private LocalDateTime dataInvio;
}
