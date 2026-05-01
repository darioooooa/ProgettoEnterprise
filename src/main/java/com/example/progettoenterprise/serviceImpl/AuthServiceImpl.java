package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.service.AuthService;
import com.example.progettoenterprise.dto.LoginDTO;
import com.example.progettoenterprise.dto.RegistrazioneDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import com.example.progettoenterprise.security.TokenStore;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;
    private final UtenteRepository utenteRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    // Metodo per registrare un nuovo utente
    @Override
    @Transactional
    public UtenteDTO registraUtente(RegistrazioneDTO regDTO){

        // Controlla se l'email esiste già
        if (utenteRepository.findByEmail(regDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException(messageLang.getMessage("auth.email.duplicate"));
        }

        // Controlla se lo username esiste già
        if (utenteRepository.findByUsername(regDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException(messageLang.getMessage("auth.username.duplicate"));
        }

        Viaggiatore nuovoViaggiatore = modelMapper.map(regDTO, Viaggiatore.class);

        nuovoViaggiatore.setRuolo(Utente.Ruolo.ROLE_VIAGGIATORE);
        // Cifratura della password
        nuovoViaggiatore.setPassword(passwordEncoder.encode(regDTO.getPassword()));

        Viaggiatore salvato = utenteRepository.save(nuovoViaggiatore);

        return modelMapper.map(salvato, UtenteDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> eseguiLogin(LoginDTO loginDTO) throws Exception{
        // Valida le credenziali
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        // Cerca l'utente
        Utente utente = utenteRepository.findByUsernameOrEmail(loginDTO.getUsername(),loginDTO.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("auth.user.notfound", loginDTO.getUsername())));

        // Crea il token, includendo le informazioni dell'utente nel payload
        String token = tokenStore.createToken(Map.of(
                "id", utente.getId().toString(),
                "username", utente.getUsername(),
                "role",utente.getRuolo().name()
        ));

        // Restituisce i dati impacchettati al controller
        return Map.of(
                "token", token,
                "id", utente.getId().toString(),
                "email", utente.getEmail(),
                "username", utente.getUsername(),
                "ruolo", utente.getRuolo().name()
        );
    }


}
