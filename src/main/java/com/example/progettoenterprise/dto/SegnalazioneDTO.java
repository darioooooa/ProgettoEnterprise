package com.example.progettoenterprise.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegnalazioneDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String stato;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataSegnalazione;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataRisoluzione;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long adminId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long segnalatoreId;

    private String tipo;
    private Long idRiferimento;
    private String motivo;
    private String descrizione;
}