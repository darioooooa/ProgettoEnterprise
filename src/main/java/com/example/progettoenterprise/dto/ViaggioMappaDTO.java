package com.example.progettoenterprise.dto;

import lombok.Data;

@Data
public class ViaggioMappaDTO {
    private Long id;
    private String titolo;
    private Double longitudine;
    private Double latitudine;
    private String urlImmagine;

}
