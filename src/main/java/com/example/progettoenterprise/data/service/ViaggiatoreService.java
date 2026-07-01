package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ViaggiatoreService {
    ViaggiatoreDTO getProfiloViaggiatore(Long id);
    ViaggiatoreDTO aggiornaProfilo(Long id, ViaggiatoreDTO viaggiatoreDTO);
    List<ViaggiatoreDTO> cercaViaggiatori(String query);//per le amicizie
    RichiestaPromozioneDTO creaRichiestaPromozione(Long viaggiatoreId, RichiestaPromozioneDTO dto, MultipartFile file);
    RichiestaPromozione trovaRichiestaPendente(Long viaggiatoreId);



}
