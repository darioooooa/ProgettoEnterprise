package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.ViaggioSpecification;
import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViaggioServiceImpl implements ViaggioService {

    // Dimensioni della pagina per la ricerca
    private static final int SIZE_FOR_PAGE = 10;

    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public ViaggioDTO creaViaggio(ViaggioDTO viaggioDTO, Long organizzatoreId) {
        Utente organizzatore = utenteRepository.findById(organizzatoreId)
                .orElseThrow(() -> {
                    log.error("Impossibile creare viaggio: utente ID {} non trovato", organizzatoreId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", organizzatoreId));
                });
        Viaggio viaggio = modelMapper.map(viaggioDTO, Viaggio.class);

        // Controllo sui dati del viaggio
        if (viaggio.getPrezzo() <= 0) {
            log.warn("Tentativo di creazione viaggio con prezzo non valido: {}", viaggio.getPrezzo());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_price"));
        }
        if (viaggio.getDataFine().isBefore(viaggio.getDataInizio()) || viaggio.getDataInizio() == null){
            log.warn("Tentativo di creazione viaggio con date non valide. Inizio: {}, Fine: {}",
                    viaggio.getDataInizio(), viaggio.getDataFine());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_date"));
        }

        viaggio.setOrganizzatore(organizzatore);
        viaggio.setMediaRecensioni(0.0);
        viaggio.setNumeroRecensioni(0);
        Viaggio salvato = viaggioRepository.save(viaggio);
        return modelMapper.map(salvato, ViaggioDTO.class);
    }

    @Override
    @Transactional
    public void eliminaViaggio(Long viaggioId, Long organizzatoreId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> {
                    log.warn("Tentativo di eliminazione fallito: viaggio ID {} inesistente", viaggioId);
                    return new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId));
                });
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)) {
            log.error("Accesso negato! L'utente ID {} ha tentato di eliminare il viaggio ID {} che appartiene all'utente ID {}",
                    organizzatoreId, viaggioId, viaggio.getOrganizzatore().getId());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }

        viaggioRepository.delete(viaggio);
    }

    // Metodo per fornire dettagli delle recensioni di un viaggio
    @Override
    @Transactional(readOnly = true)
    // Prima cerca le statistiche del viaggio nella cache
    @Cacheable(value = CacheConfig.CACHE_VIAGGI_MEDIA, key = "#viaggioId")
    public Map<String, Object> getStatisticheRecensioni(Long viaggioId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", viaggioId)));

        return Map.of(
                "viaggioId", viaggio.getId(),
                "mediaRecensioni", viaggio.getMediaRecensioni(),
                "numeroRecensioni", viaggio.getNumeroRecensioni()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ViaggioDTO> ricercaFiltrata(ViaggioSpecification.ViaggioFilter viaggioFilter, Long utenteId, int page) {

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> {
                    log.error("Impossibile creare viaggio: utente ID {} non trovato", utenteId);
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId));
                });

        // Se è un organizzatore, si forza il filtro per solo i suoi viaggi
        if (utente.getRuolo().equals(Utente.Ruolo.ROLE_ORGANIZZATORE)){
            viaggioFilter.setOrganizzatoreId(utenteId);
        }

        // Paginazione della ricerca
        PageRequest pageRequest = PageRequest.of(page, SIZE_FOR_PAGE, Sort.by("id").descending());
        Page<Viaggio> viaggiPage = viaggioRepository.findAll(ViaggioSpecification.withFilter(viaggioFilter), pageRequest);

        // Controllo sulla pagina corrente
        if ((page < 0 || page >= viaggiPage.getTotalPages()) && viaggiPage.getTotalPages() > 0) {
            log.warn("Pagina non valida: {}. Pagina totale: {}", page, viaggiPage.getTotalPages());
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_page"));
        }

        return viaggiPage.map(viaggio -> modelMapper.map(viaggio, ViaggioDTO.class));
    }
}