package com.example.progettoenterprise.dto;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViaggioDTO {
    private Long idViaggio;

    @NotBlank
    private String titolo;

    private String descrizione;

    @NotNull
    @Min(value=0)
    private Double prezzo;

    @NotNull
    @Future  //serve per far si che la data inizio non venga scelta prima della data corrente
    private LocalDate dataInizio;

    @NotNull
    private LocalDate dataFine;

    private Long organizzatoreId;
    private String organizzatoreUsername;
}

