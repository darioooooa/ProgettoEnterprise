package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaUtenteKey implements Serializable {

    @Column(name = "utente_id")
    private Long utenteId;

    @Column(name = "lista_id")
    private Long listaId;
}