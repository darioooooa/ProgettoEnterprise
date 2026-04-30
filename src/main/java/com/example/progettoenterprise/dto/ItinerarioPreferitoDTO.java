package com.example.progettoenterprise.dto;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito.Visibilita;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarioPreferitoDTO {
    private Long idItinerario;

    @NotBlank
    private String nome;

    private LocalDate dataCreazione;

    @NotNull
    private Visibilita visibilita;
}