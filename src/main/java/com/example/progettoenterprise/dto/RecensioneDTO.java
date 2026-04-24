package com.example.progettoenterprise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecensioneDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String utenteUsername;

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    private Integer voto;

    @Size(max = 500, message = "Limite di caratteri del commento superato")
    private String commento;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long viaggioId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataCreazione;
}
