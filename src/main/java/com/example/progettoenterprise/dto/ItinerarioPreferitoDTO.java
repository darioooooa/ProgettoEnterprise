package com.example.progettoenterprise.dto;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito.Visibilita;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarioPreferitoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idItinerario;

    @NotBlank
    private String nome;

    private Boolean inCondivisione;

    private String proprietarioUsername;;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate dataCreazione;

    @NotNull
    private Visibilita visibilita;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<ViaggioDTO> viaggiContenuti;
}