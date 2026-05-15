package com.example.progettoenterprise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmagineViaggioDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "L'URL dell'immagine è obbligatorio")
    @Size(max=500, message = "Limite di caratteri del link superato")
    private String url;

    @NotNull(message = "Specificare se l'immagine deve essere pubblica o privata")
    private boolean pubblica;

    private Long viaggioId;

}