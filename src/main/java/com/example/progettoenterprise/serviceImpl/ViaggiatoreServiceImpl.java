package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggiatoreRepository;
import com.example.progettoenterprise.data.service.ViaggiatoreService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViaggiatoreServiceImpl implements ViaggiatoreService {

    private final ViaggiatoreRepository viaggiatoreRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;
    private final UtenteRepository utenteRepository;
    private final RichiestaPromozioneRepository richiestaRepository;
    private final Keycloak keycloak;

    @Override
    @Transactional(readOnly = true)
    public ViaggiatoreDTO getProfiloViaggiatore(Long id) {
        Viaggiatore viaggiatore=  viaggiatoreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Recupero fallito: Impossibile recuperare il viaggiatore con id {}", id);
                    return new EntityNotFoundException( messageLang.getMessage("utente.notexist",id));
                });
        return modelMapper.map(viaggiatore, ViaggiatoreDTO.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_UTENTI_AUTH, key = "#result.email")
    public ViaggiatoreDTO aggiornaProfilo(Long id, ViaggiatoreDTO viaggiatoreDTO) {
        Viaggiatore viaggiatore= viaggiatoreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Aggiornamento fallito: Impossibile recuperare il profilo del viaggiatore con id {}", id);
                    return new EntityNotFoundException( messageLang.getMessage("utente.notexist",id));
                });
        viaggiatore.setNome(viaggiatoreDTO.getNome());
        viaggiatore.setCognome(viaggiatoreDTO.getCognome());
        Viaggiatore salvato= viaggiatoreRepository.save(viaggiatore);
        return modelMapper.map(salvato,ViaggiatoreDTO.class);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ViaggiatoreDTO> cercaViaggiatori(String query) {
        return viaggiatoreRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(v -> modelMapper.map(v, ViaggiatoreDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RichiestaPromozioneDTO creaRichiestaPromozione(Long viaggiatoreId, RichiestaPromozioneDTO dto){
        // Controlla se email e username sono già in uso
        if (utenteRepository.findByUsername(dto.getUsernameRichiesto()).isPresent()){
            log.warn("Errore nella creazione di un nuovo utente: lo username {} è già presente", dto.getUsernameRichiesto());
            throw new IllegalArgumentException(messageLang.getMessage("utente.username.exist", dto.getUsernameRichiesto()));
        }
        if (utenteRepository.findByEmail(dto.getEmailProfessionale()).isPresent()){
            log.warn("Errore nella creazione di un nuovo utente: l'email {} è già presente", dto.getEmailProfessionale());
            throw new IllegalArgumentException(messageLang.getMessage("utente.email.exist", dto.getEmailProfessionale()));
        }

        // Controlla se ci sono richieste IN_ATTESA con stesso username o email
        boolean usernameRichiestoGiaInUso = richiestaRepository.existsByUsernameRichiestoAndStato(
                dto.getUsernameRichiesto(),
                RichiestaPromozione.StatoRichiesta.IN_ATTESA
        );
        if (usernameRichiestoGiaInUso){
            log.warn("Errore nella creazione: lo username {} è già bloccato da una richiesta in attesa", dto.getUsernameRichiesto());
            throw new IllegalArgumentException(messageLang.getMessage("utente.username.exist", dto.getUsernameRichiesto()));
        }
        boolean emailRichiestaGiaInUso = richiestaRepository.existsByEmailProfessionaleAndStato(
                dto.getEmailProfessionale(),
                RichiestaPromozione.StatoRichiesta.IN_ATTESA
        );
        if (emailRichiestaGiaInUso) {
            log.warn("Errore nella creazione: l'email {} è già bloccata da una richiesta in attesa", dto.getEmailProfessionale());
            throw new IllegalArgumentException(messageLang.getMessage("utente.email.exist", dto.getEmailProfessionale()));
        }

        // Cerca lo username in keycloak
        List<UserRepresentation> keycloakUsers = keycloak.realm("enterprise-realm").users().search(dto.getUsernameRichiesto(), true);
        if(!keycloakUsers.isEmpty()){
            log.warn("Errore nella creazione di un nuovo utente su keyloak: lo username {} è già presente", dto.getUsernameRichiesto());
            throw new IllegalArgumentException(messageLang.getMessage("utente.username.exist", dto.getUsernameRichiesto()));
        }
        // Cerca l'email nel keycloak
        List<UserRepresentation> keycloakEmails = keycloak.realm("enterprise-realm").users().searchByEmail(dto.getEmailProfessionale(), true);
        if (!keycloakEmails.isEmpty()){
            log.warn("Errore nella creazione di un nuovo utente su keyloak: l'email {} è già presente", dto.getEmailProfessionale());
            throw new IllegalArgumentException(messageLang.getMessage("utente.email.exist", dto.getEmailProfessionale()));
        }

        // Salva la richiesta
        Viaggiatore viaggiatore = viaggiatoreRepository.findById(viaggiatoreId)
                .orElseThrow(() -> {
                    log.error("Impossibile creare la richiesta: l'utente con id {} non esiste", viaggiatoreId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", viaggiatoreId));
                });

        RichiestaPromozione nuovaRichiesta = new RichiestaPromozione();
        nuovaRichiesta.setViaggiatore(viaggiatore);
        nuovaRichiesta.setBiografiaProfessionale(dto.getBiografiaProfessionale());
        nuovaRichiesta.setDocumentiLink(dto.getDocumentiLink());
        nuovaRichiesta.setMotivazione(dto.getMotivazione());
        nuovaRichiesta.setUsernameRichiesto(dto.getUsernameRichiesto());
        nuovaRichiesta.setEmailProfessionale(dto.getEmailProfessionale());
        nuovaRichiesta.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA);

        RichiestaPromozione salvata = richiestaRepository.save(nuovaRichiesta);
        return modelMapper.map(salvata, RichiestaPromozioneDTO.class);
    }

}
