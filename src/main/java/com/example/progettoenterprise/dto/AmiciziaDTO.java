package com.example.progettoenterprise.dto;

import com.example.progettoenterprise.data.entities.Amicizia.StatoAmicizia;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmiciziaDTO {

    private Long id;

    private Long richiedenteId;
    private String richiedenteUsername;

    private Long riceventeId;
    private String riceventeUsername;

    private StatoAmicizia stato;
    private LocalDateTime dataRichiesta;
    private LocalDateTime dataRisposta;
}