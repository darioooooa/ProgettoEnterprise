package com.example.progettoenterprise.dto;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrenotazioneDTO {
    private Long idPrenotazione;
    private LocalDateTime dataPrenotazione;

    @NotNull
    @Min(value=1)
    private Integer numeroPersone;

    private Long viaggiatoreId;
    private String viaggiatoreUsername;

    @NotNull
    private Long viaggioId;
    private String viaggioTitolo;
}
