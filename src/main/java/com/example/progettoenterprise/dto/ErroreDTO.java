package com.example.progettoenterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Classe che rappresenta un errore
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErroreDTO {

    // Codice di stato HTTP
    private int stato;

    // Messaggio dell'errore
    private String messaggio;

    // Momento in cui si è verificato l'errore
    private long timestamp;
}
