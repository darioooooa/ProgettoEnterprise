package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import com.example.progettoenterprise.config.i18n.MessageLang;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtenteServiceImpl implements UtenteService {
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    public UtenteDTO getProfiloById(Long id) {
        Utente utente = utenteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", id)));
        return modelMapper.map(utente, UtenteDTO.class);
    }
    @Override
    @Transactional
    public UtenteDTO aggiornaProfilo(Long id, UtenteDTO utenteDto) {
        return utenteRepository.findById(id).map(utente -> {
            utente.setNome(utenteDto.getNome());
            utente.setCognome(utenteDto.getCognome());
            // Aggiungere altri campi se necessario

            Utente salvato = utenteRepository.save(utente);
            return modelMapper.map(salvato, UtenteDTO.class);
        }).orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", id)));
    }

    @Override
    public List<UtenteDTO> cercaUtenti(String query) {
        return utenteRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(u -> modelMapper.map(u, UtenteDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminaAccount(Long id) {
        if (!utenteRepository.existsById(id)) {
            throw new EntityNotFoundException(messageLang.getMessage("utente.notexist", id));
        }
        utenteRepository.deleteById(id);
    }
}
