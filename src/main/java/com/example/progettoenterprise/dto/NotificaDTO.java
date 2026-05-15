package com.example.progettoenterprise.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    private String messaggio;

    private boolean isLetta=false;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String dataCreazione;

    private Long idRiferimento;
}