package com.example.progettoenterprise.dto;
import com.example.progettoenterprise.data.entities.Utente.Ruolo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String nomeCompleto;

    //Niente password per motivi di sicurezza
    //Niente liste per evitare loop infiniti
}