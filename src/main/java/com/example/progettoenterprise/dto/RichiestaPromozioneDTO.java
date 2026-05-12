package com.example.progettoenterprise.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RichiestaPromozioneDTO {
    private Long id;
    private String usernameViaggiatore;
    private String emailViaggiatore;
    private String biografiaProfessionale;
    private String documentiLink;
    private Long adminId;
    private LocalDateTime dataRichiesta;
    private String motivazione;
    private String stato;
}
