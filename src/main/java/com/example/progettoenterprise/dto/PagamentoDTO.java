package com.example.progettoenterprise.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {

    private Long id;

    @NotBlank(message = "Il titolare della carta è obbligatorio")
    private String titolareCarta;

    @NotBlank(message = "Il numero della carta è obbligatorio")
    @Size(min = 16, max = 16, message = "Il numero della carta deve essere di 16 cifre")
    private String numeroCarta;

    @NotBlank(message = "La scadenza è obbligatoria")
    private String dataScadenza;

    private String circuito;

    // Campo opzionale per mostrare la carta in modo sicuro(*******1111)
    private String numeroOscurato;


    private Long idViaggiatore;


}