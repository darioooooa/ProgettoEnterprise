package com.example.progettoenterprise.dto;
import com.example.progettoenterprise.data.entities.Utente.Ruolo;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteDTO {
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

    private Ruolo ruolo;

    private String nomeCompleto;

    //Niente password per motivi di sicurezza
    //Niente liste per evitare loop infiniti
}
