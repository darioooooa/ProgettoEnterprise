package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
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
    private final Keycloak keycloak;
    private final OrganizzatoreRepository organizzatoreRepository;
    private final RichiestaPromozioneRepository richiestaPromozioneRepository;

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
    public RichiestaPromozioneDTO creaRichiestaPromozione(Long viaggiatoreId, RichiestaPromozioneDTO dto) {

        Viaggiatore utenteAttuale = viaggiatoreRepository.findById(viaggiatoreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(viaggiatoreId, RichiestaPromozione.StatoRichiesta.IN_ATTESA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai già una richiesta in fase di valutazione.");
        }
        if (richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(viaggiatoreId, RichiestaPromozione.StatoRichiesta.APPROVATA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sei già un organizzatore approvato!");
        }

        if (richiestaPromozioneRepository.existsByUsernameRichiestoAndStatoNot(dto.getUsernameRichiesto(), RichiestaPromozione.StatoRichiesta.RIFIUTATA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questo username è attualmente richiesto da un'altra candidatura in corso.");
        }
        if (!utenteAttuale.getUsername().equals(dto.getUsernameRichiesto()) &&
                utenteRepository.existsByUsername(dto.getUsernameRichiesto())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questo username è già in uso da un altro utente. Scegline un altro.");
        }

        if (richiestaPromozioneRepository.existsByEmailProfessionaleAndStatoNot(dto.getEmailProfessionale(), RichiestaPromozione.StatoRichiesta.RIFIUTATA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questa email è in fase di valutazione in un'altra richiesta.");
        }
        if (!utenteAttuale.getEmail().equals(dto.getEmailProfessionale()) &&
                utenteRepository.existsByEmail(dto.getEmailProfessionale())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questa email è già associata ad un altro account.");
        }

        RichiestaPromozione nuovaRichiesta = new RichiestaPromozione();
        nuovaRichiesta.setViaggiatore(utenteAttuale);
        nuovaRichiesta.setUsernameRichiesto(dto.getUsernameRichiesto());
        nuovaRichiesta.setEmailProfessionale(dto.getEmailProfessionale());
        nuovaRichiesta.setMotivazione(dto.getMotivazione());
        nuovaRichiesta.setBiografiaProfessionale(dto.getBiografiaProfessionale());
        nuovaRichiesta.setDocumentiLink(dto.getDocumentiLink());
        nuovaRichiesta.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA);
        nuovaRichiesta.setDataRichiesta(LocalDateTime.now());

        RichiestaPromozione salvata = richiestaPromozioneRepository.save(nuovaRichiesta);

        return modelMapper.map(salvata, RichiestaPromozioneDTO.class);
    }

    public RichiestaPromozione trovaRichiestaPendente(Long viaggiatoreId) {
        return richiestaPromozioneRepository.findByViaggiatoreIdAndStato(viaggiatoreId, RichiestaPromozione.StatoRichiesta.IN_ATTESA)
                .orElse(null);
    }
}
