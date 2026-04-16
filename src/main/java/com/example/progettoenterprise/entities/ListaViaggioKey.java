package com.example.progettoenterprise.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaViaggioKey implements Serializable {
    @Column(name = "lista_id")
    private Long listaId;

    @Column(name = "viaggio_id")
    private Long viaggioId;
}
