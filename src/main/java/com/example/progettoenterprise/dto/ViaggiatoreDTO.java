package com.example.progettoenterprise.dto;
import lombok.*;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)

public class ViaggiatoreDTO extends UtenteDTO{
    private List<PrenotazioneDTO> miePrenotazioni;
}
