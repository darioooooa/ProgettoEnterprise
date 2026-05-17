package com.example.progettoenterprise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrazioneDTO {

    @NotBlank
    @Size(max = 50, message = "Lo username non può superare i 50 caratteri")
    private String username;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Size(max = 50, message = "L'email può contenere al massimo 50 caratteri")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min=6, message = "La password deve contenere almeno 6 caratteri")
    private String password;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 50, message = "Il nome non può superare i 50 caratteri")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(max = 50, message = "Il cognome non può superare i 50 caratteri")
    private String cognome;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String ruolo;
}
