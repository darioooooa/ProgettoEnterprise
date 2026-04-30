package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import java.util.List;
public interface ViaggiatoreService {
    ViaggiatoreDTO getProfiloViaggiatore(Long id);
    ViaggiatoreDTO aggiornaProfilo(Long id, ViaggiatoreDTO viaggiatoreDTO);
    List<ViaggiatoreDTO> cercaViaggiatori(String query);//per le amicizie




}
