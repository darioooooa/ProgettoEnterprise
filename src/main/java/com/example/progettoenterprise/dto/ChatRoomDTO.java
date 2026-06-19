package com.example.progettoenterprise.dto;

import lombok.*;

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
}