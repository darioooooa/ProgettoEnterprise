package com.example.progettoenterprise.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViaggioDTO {
    // I campi marcati con READ_ONLY servono per evitare che vengano modificati dall'utente

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Il titolo è obbligatorio")
    private String titolo;

    private String descrizione;

    @NotBlank(message = "La destinazione è obbligatoria")
    private String destinazione;

    @NotNull(message = "Il prezzo è obbligatorio")
    @Min(value = 0, message = "Il prezzo non può essere negativo")
    private Double prezzo;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Future(message = "La data di inizio deve essere nel futuro")  //serve per far si che la data inizio non venga scelta prima della data corrente
    private LocalDateTime dataInizio;

    @NotNull(message = "La data di fine è obbligatoria")
    @Future(message = "La data di fine deve essere nel futuro")
    private LocalDateTime dataFine;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double mediaRecensioni;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer numeroRecensioni;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long organizzatoreId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String organizzatoreUsername;
}

