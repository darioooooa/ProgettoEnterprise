package com.example.progettoenterprise.dto;

import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaViaggioDTO {
    @JsonIgnore
    private ItinerarioPreferito itinerarioPreferito;

    private ViaggioDTO viaggio;
}
