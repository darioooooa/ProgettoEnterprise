package com.example.progettoenterprise.dto;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaDTO {
    private Long id;
    @NotBlank
    private String messaggio;

    private boolean isLetta=false;
    private String dataCreazione;
    private Long idRiferimento;

}
