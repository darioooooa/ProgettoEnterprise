package com.example.progettoenterprise.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrenotazioneDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataPrenotazione;

    @NotNull(message = "Devi specificare il numero di persone")
    @Min(value=1, message = "Devi specificare un numero maggiore di 0")
    private Integer numeroPersone;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long viaggiatoreId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String viaggiatoreUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long viaggioId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String viaggioTitolo;
}
