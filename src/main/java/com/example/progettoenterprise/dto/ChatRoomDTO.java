package com.example.progettoenterprise.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {
    private Long id;
    private Long viaggioId;
    private String titoloViaggio;
    private String viaggiatoreUsername;
    private String organizzatoreUsername;
    private int messaggiNonLetti;
    private LocalDateTime dataUltimoMessaggio;
}