package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{


    private final UtenteRepository utenteRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    // Metodo per registrare un nuovo utente
    @Override
    @Transactional
    public UtenteDTO registraUtente(Utente utente){

        // Controlla se l'email esiste già
        if (utenteRepository.findByEmail(utente.getEmail()).isPresent()) {
            throw new RuntimeException(messageLang.getMessage("auth.email.duplicate"));
        }

        // Controlla se lo username esiste già
        if (utenteRepository.findByUsername(utente.getUsername()).isPresent()) {
            throw new RuntimeException(messageLang.getMessage("auth.username.duplicate"));
        }

        // Cifratura della password
        utente.setPassword(passwordEncoder.encode(utente.getPassword()));

        // Impostazione del ruolo predefinito
        utente.setRuolo(Utente.Ruolo.VIAGGIATORE);

        Utente salvato = utenteRepository.save(utente);

        return modelMapper.map(salvato, UtenteDTO.class);
    }

    // Metodo per ottenere un utente dallo username o dall'email
    @Override
    public Utente getUtenteByUsernameOrEmail(String identificativo){
        return utenteRepository.findByUsernameOrEmail(identificativo,identificativo)
                .orElseThrow(() -> new RuntimeException(messageLang.getMessage("auth.user.notfound", identificativo)));
    }
}
