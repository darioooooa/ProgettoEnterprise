package com.example.progettoenterprise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttivitaViaggioDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank
    private String titolo;

    private String descrizione;
    @NotNull
    private LocalDateTime orarioInizio;
    @NotNull
    private LocalDateTime orarioFine;
    @NotBlank
    private String posizione;

    @Min(value=0)
    private Double costo;

}
