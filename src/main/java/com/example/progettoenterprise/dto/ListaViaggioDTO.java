package com.example.progettoenterprise.dto;

import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.Viaggio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaViaggioDTO {
    private ItinerarioPreferito itinerarioPreferito;

    private Viaggio viaggio;
}
