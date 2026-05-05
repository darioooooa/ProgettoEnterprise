package com.example.progettoenterprise.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Classe che rappresenta un errore
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErroreDTO {

    // Codice di stato HTTP
    private int stato;

    // Messaggio dell'errore
    private String messaggio;

    // URI della richiesta che ha causato l'errore
    private String uri;

    // Momento in cui si è verificato l'errore
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
