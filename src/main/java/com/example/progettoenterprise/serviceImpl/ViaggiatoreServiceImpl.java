package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.ViaggiatoreRepository;
import com.example.progettoenterprise.data.service.ViaggiatoreService;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ViaggiatoreServiceImpl implements ViaggiatoreService {

    private final ViaggiatoreRepository viaggiatoreRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    public ViaggiatoreDTO getProfiloViaggiatore(Long id) {
        Viaggiatore viaggiatore=  viaggiatoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException( messageLang.getMessage("utente.notexist",id)));



        return modelMapper.map(viaggiatore, ViaggiatoreDTO.class);
    }

    @Override
    @Transactional
    public ViaggiatoreDTO aggiornaProfilo(Long id, ViaggiatoreDTO viaggiatoreDTO) {
        Viaggiatore viaggiatore= viaggiatoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException( messageLang.getMessage("utente.notexist",id)));
        viaggiatore.setNome(viaggiatoreDTO.getNome());
        viaggiatore.setCognome(viaggiatoreDTO.getCognome());
        Viaggiatore salvato= viaggiatoreRepository.save(viaggiatore);
        return modelMapper.map(salvato,ViaggiatoreDTO.class);

    }

    @Override
    public List<ViaggiatoreDTO> cercaViaggiatori(String query) {
        return viaggiatoreRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(v -> modelMapper.map(v, ViaggiatoreDTO.class))
                .collect(Collectors.toList());
    }
}
