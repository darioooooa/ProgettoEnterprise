package com.example.progettoenterprise.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaPromozioneDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String usernameViaggiatore;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String emailViaggiatore;

    @NotBlank(message = "La biografia professionale è obbligatoria")
    private String biografiaProfessionale;

    @NotBlank(message = "Inserisci un link valido per i tuoi documenti (CV/Portfolio)")
    private String documentiLink;

    private String motivazione;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long adminId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataRichiesta;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String stato;

    @NotBlank(message = "Lo username professionale è obbligatorio")
    private String usernameRichiesto;

    @NotBlank(message = "L'email professionale è obbligatoria")
    @Email(message = "Inserisci un indirizzo email valido")
    private String emailProfessionale;
}