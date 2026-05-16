package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListaViaggioKey implements Serializable {
    @Column(name = "lista_id")
    private Long listaId;

    @Column(name = "viaggio_id")
    private Long viaggioId;
}