package com.example.progettoenterprise.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttivitaViaggioDTO {
    private Long id;
    @NotBlank
    private String titolo;

    private String descrizione;
    @NotNull
    private LocalDateTime orarioInizio;
    @NotNull
    private LocalDateTime orarioFine;

    @Min(value=0)
    private Double costo;

    @NotNull
    private Long viaggioId;

}
