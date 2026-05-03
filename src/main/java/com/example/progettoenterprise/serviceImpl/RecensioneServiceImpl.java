package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Recensione;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.RecensioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.RecensioneService;
import com.example.progettoenterprise.dto.RecensioneDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecensioneServiceImpl implements RecensioneService {
    private final RecensioneRepository recensioneRepository;
    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    private final MessageLang messageLang;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    // Invalida la cache del viaggio
    @CacheEvict(value = CacheConfig.CACHE_VIAGGI_MEDIA, key = "#viaggioId")
    public RecensioneDTO aggiungiRecensione(String username, Long viaggioId, int voto, String commento) {
        // Controllo voto valido
        if (voto < 1 || voto > 5) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.invalid_rating"));
        }

        Utente autore = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("utente.notfound", username)));

        // Controllo utente prenotato nel viaggio che recensisce
        boolean haDiritto = prenotazioneRepository.existsByViaggiatoreIdAndViaggioIdAndStato(autore.getId(), viaggioId, Prenotazione.StatoPrenotazione.CONFERMATA);
        if(!haDiritto){
            throw new IllegalArgumentException(messageLang.getMessage("recensione.unauthorized_prenotazione"));
        }
        // Controlla se l'utente ha già inserito una recensione per quel viaggio
        boolean isPresent = recensioneRepository.existsByViaggioIdAndUtenteId(viaggioId, autore.getId());
        if (isPresent) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.already_exists"));
        }

        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId)));

        Recensione recensione = new Recensione();
        recensione.setUtente(autore);
        recensione.setViaggio(viaggio);
        recensione.setVoto(voto);
        recensione.setCommento(commento);
        Recensione salvata = recensioneRepository.save(recensione);

        // Aggiorna la media del viaggio
        viaggioRepository.aggiornaStatisticheRecensione(viaggioId,voto);

        return modelMapper.map(salvata, RecensioneDTO.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_VIAGGI_MEDIA, key = "#viaggioId")
    public void eliminaRecensione(Long viaggioId, Long recensioneId, String username) {
        Recensione recensione = recensioneRepository.findById(recensioneId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("recensione.notexist", recensioneId)));

        // Controllo che la recensione appartenga al viaggio specificato
        if (!recensione.getViaggio().getId().equals(viaggioId)) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.not_part_of_viaggio"));
        }

        // Controllo che l'utente sia l'autore o l'organizzatore del viaggio
        boolean isAutore = recensione.getUtente().getUsername().equals(username);
        boolean isOrganizzatore = recensione.getViaggio().getOrganizzatore().getUsername().equals(username);
        if (!isAutore && !isOrganizzatore) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.unauthorized_utente"));
        }

        int votoSottratto = recensione.getVoto();
        recensioneRepository.delete(recensione);
        // Aggiorna la media del viaggio
        viaggioRepository.ricalcolaMediaPerEliminazione(viaggioId, votoSottratto);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_VIAGGI_MEDIA, key = "#viaggioId")
    public RecensioneDTO aggiornaRecensione(Long viaggioId, Long recensioneId, String username, int nuovoVoto, String nuovoCommento) {
        // Controllo voto valido
        if (nuovoVoto < 1 || nuovoVoto > 5) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.invalid_rating"));
        }
        // Controllo che l'utente sia l'autore della recensione
        Recensione recensione = recensioneRepository.findById(recensioneId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("recensione.notexist", recensioneId)));
        if (!recensione.getUtente().getUsername().equals(username)) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.unauthorized_utente"));
        }

        // Controllo che la recensione appartenga al viaggio specificato
        if (!recensione.getViaggio().getId().equals(viaggioId)) {
            throw new IllegalArgumentException(messageLang.getMessage("recensione.not_part_of_viaggio"));
        }

        int votoVecchio = recensione.getVoto();
        recensione.setVoto(nuovoVoto);
        recensione.setCommento(nuovoCommento);

        Recensione aggiornata = recensioneRepository.save(recensione);

        // Aggiorna la media del viaggio se il voto è stato modificato
        if(nuovoVoto != votoVecchio){
            viaggioRepository.ricalcolaMediaPerModifica(viaggioId, votoVecchio, nuovoVoto);
        }

        return modelMapper.map(aggiornata, RecensioneDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecensioneDTO> getRecensioniViaggio(Long viaggioId) {
        if (!viaggioRepository.existsById(viaggioId)) {
            throw new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId));
        }
        return recensioneRepository.findByViaggioId(viaggioId).stream()
                .map(recensione -> modelMapper.map(recensione, RecensioneDTO.class))
                .collect(Collectors.toList());
    }

}
