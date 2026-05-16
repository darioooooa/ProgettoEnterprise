package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.specifications.UtenteSpecification;
import com.example.progettoenterprise.data.service.UtenteService;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import com.example.progettoenterprise.config.i18n.MessageLang;

@Service
@RequiredArgsConstructor
public class UtenteServiceImpl implements UtenteService {

    // Dimensione della pagina di ricerca
    private static final int SIZE_FOR_PAGE = 10;

    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UtenteDTO getProfiloById(Long id) {
        Utente utente = utenteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", id)));
        return modelMapper.map(utente, UtenteDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UtenteDTO findByUsername(String username) {
        // Cerca l'utente tramite il nickname, altrimenti lancia l'eccezione i18n
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.username_notexist", username)));

        // Converte l'entità database nel DTO per il frontend
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
    @Transactional
    public void aggiornaPassword(Long id, String vecchiaPassword, String nuovaPassword){
        Utente utente = utenteRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException(messageLang.getMessage("utente.notexist", id))
                );
        //Controllo se la vecchia password corrisponde
        if (!passwordEncoder.matches(vecchiaPassword, utente.getPassword())) {
            throw new IllegalArgumentException(messageLang.getMessage("password.must_be_as_old"));
        }


        if (vecchiaPassword.equals(nuovaPassword)) {
            throw new IllegalArgumentException(messageLang.getMessage("password.same_as_old"));
        }
        String passwordCriptata=passwordEncoder.encode(nuovaPassword);
        utente.setPassword(passwordCriptata);
        utenteRepository.save(utente);

    }

    @Override
    @Transactional
    public void eliminaAccount(Long id) {
        if (!utenteRepository.existsById(id)) {
            throw new EntityNotFoundException(messageLang.getMessage("utente.notexist", id));
        }
        utenteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UtenteDTO> ricercaUtenti(UtenteSpecification.UtenteFilter utenteFilter, int page) {

        // Creazione della richiesta di paginazione
        PageRequest pageRequest = PageRequest.of(page, SIZE_FOR_PAGE,
                Sort.by("cognome").ascending().and(Sort.by("nome").ascending()));
        Page<Utente> utentiPage = utenteRepository.findAll(UtenteSpecification.withFilter(utenteFilter), pageRequest);

        // Controllo sulla pagina corrente
        if ((page < 0 || page >= utentiPage.getTotalPages()) && utentiPage.getTotalPages() > 0) {
            throw new IllegalArgumentException(messageLang.getMessage("utente.invalid_page"));
        }

        return utentiPage.map(utente -> modelMapper.map(utente, UtenteDTO.class));
    }
}
