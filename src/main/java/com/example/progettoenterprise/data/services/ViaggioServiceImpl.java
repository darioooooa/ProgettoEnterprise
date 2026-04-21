package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.dto.ViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViaggioServiceImpl implements ViaggioService {

    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public ViaggioDTO creaViaggio(ViaggioDTO viaggioDTO, Long organizzatoreId) {

        Utente organizzatore = utenteRepository.findById(organizzatoreId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notexist", organizzatoreId)));
        Viaggio viaggio = modelMapper.map(viaggioDTO, Viaggio.class);
        viaggio.setOrganizzatore(organizzatore);
        Viaggio salvato = viaggioRepository.save(viaggio);
        return modelMapper.map(salvato, ViaggioDTO.class);
    }

    @Override
    public List<ViaggioDTO> getViaggiPerOrganizzatore(Long organizzatoreId) {
        return viaggioRepository.findViaggioByOrganizzatoreId(organizzatoreId)
                .stream()
                .map(v -> modelMapper.map(v, ViaggioDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminaViaggio(Long viaggioId, Long organizzatoreId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", viaggioId)));
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)) {
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }

        viaggioRepository.delete(viaggio);
    }
}