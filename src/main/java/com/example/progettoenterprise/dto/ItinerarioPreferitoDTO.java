package com.example.progettoenterprise.dto;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito.Visibilita;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarioPreferitoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idItinerario;

    @NotBlank
    private String nome;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate dataCreazione;

    @NotNull
    private Visibilita visibilita;
}