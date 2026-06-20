package com.example.progettoenterprise.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "L'ID della prenotazione è obbligatorio")
    private Long idPrenotazione;

    @NotNull(message = "L'importo è obbligatorio")
    @Positive(message = "L'importo deve essere maggiore di zero")
    private BigDecimal importo;

    @NotNull(message = "Il codice di transazione Stripe è obbligatorio")
    private String ricevutaPagamento;

    @NotBlank(message = "Il titolare della carta è obbligatorio")
    private String titolareCarta;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String statoPagamento;


}