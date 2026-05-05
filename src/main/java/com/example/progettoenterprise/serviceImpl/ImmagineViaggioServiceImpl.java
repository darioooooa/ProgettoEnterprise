package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.ImmagineViaggio;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ImmagineViaggioRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.ImmagineViaggioService;
import com.example.progettoenterprise.dto.ImmagineViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImmagineViaggioServiceImpl implements ImmagineViaggioService {

    private final ViaggioRepository viaggioRepository;
    private final ImmagineViaggioRepository immagineRepository;
    private final MessageLang messageLang;
    private final ModelMapper modelMapper;

    // Limite massimo di immagini per viaggio
    private static final int MAX_IMMAGINI_PER_VIAGGIO = 20;

    // Estrae l'id del file di Google Drive dall'url e lo trasforma in un link diretto all'immagine
    private String convertiInLinkDiretto(String url) {
        // Regex per trovare l'id del file tra /d/ e /view, o dopo id=
        String regex = "(?:/d/|id=)([a-zA-Z0-9_-]{25,})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String fileId = matcher.group(1);
            // Restituisce il formato interpretato correttamente come immagine
            return "https://drive.google.com/uc?export=view&id=" + fileId;
        }

        log.warn("Tentativo di conversione fallito per URL non valido: {}", url);
        throw new IllegalArgumentException(messageLang.getMessage("immagine.invalid_url"));
    }

    @Override
    @Transactional
    // Aggiunge una nuova immagine al viaggio specificato
    // Bisogna assicurarsi il file drive del link sia impostato su chiunque abbia l'accesso, altrimenti
    // non sarà visibile (frontend)
    public ImmagineViaggioDTO aggiungiImmagine(Long viaggioId, String url, boolean pubblica, Long organizzatoreId) {
        // Trasforma il link in formato diretto prima di salvarlo
        String urlDiretto = convertiInLinkDiretto(url);

        // Controllo sul numero di immagini massime che si può aggiungere a un viaggio
        long numImmagini = immagineRepository.countByViaggioId(viaggioId);
        if (numImmagini >= MAX_IMMAGINI_PER_VIAGGIO) {
            log.warn("Limite immagini raggiunto ({}) per il viaggio ID: {}. Caricamento rifiutato.", MAX_IMMAGINI_PER_VIAGGIO, viaggioId);
            throw new IllegalArgumentException(messageLang.getMessage("immagine.max_reached", MAX_IMMAGINI_PER_VIAGGIO));
        }

        // Controlla che l'utente sia l'organizzatore del viaggio
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId)));
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)){
            log.error("Accesso negato: l'utente ID {} ha tentato di aggiungere un'immagine al viaggio ID {} senza autorizzazione", organizzatoreId, viaggioId);
            throw new IllegalArgumentException(messageLang.getMessage("immagine.unauthorized_utente"));
        }

        ImmagineViaggio nuovaImmagine = new ImmagineViaggio();
        nuovaImmagine.setUrl(urlDiretto);
        nuovaImmagine.setPubblica(pubblica);
        nuovaImmagine.setViaggio(viaggio);

        ImmagineViaggio salvata = immagineRepository.save(nuovaImmagine);

        return modelMapper.map(salvata, ImmagineViaggioDTO.class);
    }

    @Transactional
    @Override
    public void eliminaImmagine(Long viaggioId, Long immagineId, Long organizzatoreId){
        ImmagineViaggio immagineViaggio = immagineRepository.findById(immagineId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("immagine.notexist", immagineId)));
        // Controlla che l'immagine appartenga al viaggio specificato
        if (!immagineViaggio.getViaggio().getId().equals(viaggioId)){
            throw new IllegalArgumentException(messageLang.getMessage("immagine.not_part_of_viaggio"));
        }

        // Controlla che l'utente sia l'organizzatore del viaggio
        Viaggio viaggio = immagineViaggio.getViaggio();
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)){
            log.error("Tentativo di eliminazione non autorizzato per l'immagine ID {} da parte dell'utente ID {}", immagineId, organizzatoreId);
            throw new IllegalArgumentException(messageLang.getMessage("immagine.unauthorized_utente"));
        }

        immagineRepository.deleteById(immagineId);
    }

    @Transactional
    @Override
    public ImmagineViaggioDTO modificaVisibilita(Long viaggioId, Long immagineId, boolean nuovaVisibilita, Long organizzatoreId){
        ImmagineViaggio immagine = immagineRepository.findById(immagineId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("immagine.notexist", immagineId)));
        // Controlla che l'immagine appartenga al viaggio specificato
        if (!immagine.getViaggio().getId().equals(viaggioId)){
            throw new IllegalArgumentException(messageLang.getMessage("immagine.not_part_of_viaggio"));
        }

        Viaggio viaggio = immagine.getViaggio();
        // Controlla che l'utente sia l'organizzatore del viaggio
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)){
            throw new IllegalArgumentException(messageLang.getMessage("immagine.unauthorized_utente"));
        }

        immagine.setPubblica(nuovaVisibilita);
        ImmagineViaggio aggiornata = immagineRepository.save(immagine);
        return modelMapper.map(aggiornata, ImmagineViaggioDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImmagineViaggioDTO> getGalleriaViaggio(Long viaggioId, Long utenteId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId)));

        // Restituisce l'intera galleria se si è l'organizzatore del viaggio, altrimenti solo quelle pubbliche
        if(viaggio.getOrganizzatore().getId().equals(utenteId)){
            return immagineRepository.findByViaggioId(viaggioId)
                    .stream()
                    .map(img -> modelMapper.map(img, ImmagineViaggioDTO.class)).toList();
        }else{
            return immagineRepository.findByViaggioIdAndPubblicaTrue(viaggioId)
                    .stream()
                    .map(img -> modelMapper.map(img, ImmagineViaggioDTO.class)).toList();
        }
    }

}
