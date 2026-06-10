package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.specifications.UtenteSpecification;
import com.example.progettoenterprise.data.service.UtenteService;
import com.example.progettoenterprise.dto.UtenteDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import com.example.progettoenterprise.config.i18n.MessageLang;

// --- NUOVI IMPORT PER KEYCLOAK ---
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtenteServiceImpl implements UtenteService {

    // Dimensione della pagina di ricerca
    private static final int SIZE_FOR_PAGE = 10;

    private static final String REALM_NAME = "enterprise-realm";

    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    private final Keycloak keycloak;

    @Override
    public UtenteDTO getProfiloById(Long id) {
        Utente utente = utenteRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Recupero fallito: Impossibile recuperare l'utente con id {}", id);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", id));
                });
        return modelMapper.map(utente, UtenteDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UtenteDTO findByUsername(String username) {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Recupero fallito: Impossibile recuperare l'utente con username {}", username);
                    return new EntityNotFoundException(messageLang.getMessage("utente.username_notexist", username));
                });

        return modelMapper.map(utente, UtenteDTO.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_UTENTI_AUTH, key = "#result.email")
    public UtenteDTO aggiornaProfilo(Long id, UtenteDTO utenteDto) {
        return utenteRepository.findById(id).map(utente -> {
            utente.setNome(utenteDto.getNome());
            utente.setCognome(utenteDto.getCognome());
            // Aggiungere altri campi se necessario

            Utente salvato = utenteRepository.save(utente);
            return modelMapper.map(salvato, UtenteDTO.class);
        }).orElseThrow(() -> {
            log.warn("Aggiornamento fallito: Impossibile aggiornare l'utente con id {}, perchè non presente", id);
            return new EntityNotFoundException(messageLang.getMessage("utente.notexist", id));
        });
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_UTENTI_AUTH, key = "#result.email")
    public void eliminaAccount(Long id) {
        if (!utenteRepository.existsById(id)) {
            log.warn("Eliminazione fallita: Impossibile eliminare l'utente con id {} perchè non presente", id);
            throw new EntityNotFoundException(messageLang.getMessage("utente.notexist", id));
        }
        utenteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UtenteDTO> ricercaUtenti(UtenteSpecification.UtenteFilter utenteFilter, int page) {
        PageRequest pageRequest = PageRequest.of(page, SIZE_FOR_PAGE,
                Sort.by("cognome").ascending().and(Sort.by("nome").ascending()));
        Page<Utente> utentiPage = utenteRepository.findAll(UtenteSpecification.withFilter(utenteFilter), pageRequest);

        if ((page < 0 || page >= utentiPage.getTotalPages()) && utentiPage.getTotalPages() > 0) {
            log.warn("Pagina non valida: {}. Pagina totale: {}", page, utentiPage.getTotalPages());
            throw new IllegalArgumentException(messageLang.getMessage("utente.invalid_page"));
        }

        return utentiPage.map(utente -> modelMapper.map(utente, UtenteDTO.class));
    }


    @Override
    @Transactional
    public void inviaEmailRecuperoPassword(String email) {
        Utente utenteDb = utenteRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Richiesta fallita: l'email '{}' non esiste nei nostri archivi", email);
                    return new IllegalArgumentException(messageLang.getMessage("auth.keycloak.email_not_found", email));
                });

        if (utenteDb.getUltimoRecuperoPassword() != null) {
            long orePassate = java.time.temporal.ChronoUnit.HOURS.between(utenteDb.getUltimoRecuperoPassword(), java.time.LocalDateTime.now());

            if (orePassate < 24) {
                log.warn("L'utente {} ha già richiesto il recupero di recente.", email);
                throw new IllegalArgumentException("Hai già richiesto un cambio password di recente. Riprova tra 24 ore.");
            }
        }

        List<UserRepresentation> existingUsers = keycloak.realm(REALM_NAME).users().searchByEmail(email, true);

        if (existingUsers.isEmpty()) {
            log.warn("Richiesta recupero password fallita: l'email '{}' non esiste nei nostri archivi", email);
            throw new IllegalArgumentException(messageLang.getMessage("auth.keycloak.email_not_found", email));
        }

        String userId = existingUsers.get(0).getId();

        try {
            keycloak.realm(REALM_NAME).users().get(userId).executeActionsEmail(List.of("UPDATE_PASSWORD"));
            log.info("Istruzioni per il recupero password inviate con successo a: {}", email);
            utenteDb.setUltimoRecuperoPassword(java.time.LocalDateTime.now());
            utenteRepository.save(utenteDb);

        } catch (Exception e) {
            log.error("C'è stato un problema durante l'invio dell'email per l'utente ID: {}", userId, e);
            throw new RuntimeException("Al momento non è possibile inviare l'email. Riprova più tardi.");
        }
    }
}