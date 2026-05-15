package com.example.progettoenterprise.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
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

    private LocalDate dataInizio;

    @NotNull(message = "La data di fine è obbligatoria")
    @Future(message = "La data di fine deve essere nel futuro")
    private LocalDate dataFine;

    @NotNull
    @Min(value = -180, message = "La longitudine deve essere compresa tra -180 e 180")
    @Max(value = 180, message = "La longitudine deve essere compresa tra -180 e 180")
    private Double longitudine;

    @NotNull
    @Min(value = -90, message = "La latitudine deve essere compresa tra -90 e 90")
    @Max(value = 90, message = "La latitudine deve essere compresa tra -90 e 90")
    private Double latitudine;



    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double mediaRecensioni;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer numeroRecensioni;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long organizzatoreId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String organizzatoreUsername;
}

