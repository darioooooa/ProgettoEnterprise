package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Segnalazione;
import com.example.progettoenterprise.data.repositories.SegnalazioneRepository;
import com.example.progettoenterprise.data.repositories.specifications.SegnalazioneSpecification;
import com.example.progettoenterprise.data.service.SegnalazioneService;
import com.example.progettoenterprise.dto.SegnalazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SegnalazioneServiceImpl implements SegnalazioneService {

    private static final int DIMENSIONE_PAGINA = 10;

    private final SegnalazioneRepository segnalazioneRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public SegnalazioneDTO creaSegnalazione(SegnalazioneDTO segnalazioneDTO, Long idSegnalatore) {
        Segnalazione segnalazione = modelMapper.map(segnalazioneDTO, Segnalazione.class);
        segnalazione.setStato(Segnalazione.StatoSegnalazione.APERTA);
        segnalazione.setSegnalatoreId(idSegnalatore);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        log.info("Nuova segnalazione creata con successo con ID: {}", salvata.getId());

        return modelMapper.map(salvata, SegnalazioneDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SegnalazioneDTO> cercaSegnalazioni(SegnalazioneSpecification.SegnalazioneFilter filtro, int pagina) {
        PageRequest richiestaPagina = PageRequest.of(pagina, DIMENSIONE_PAGINA, Sort.by("dataSegnalazione").ascending());
        Page<Segnalazione> paginaSegnalazioni = segnalazioneRepository.findAll(SegnalazioneSpecification.withFilter(filtro), richiestaPagina);

        if ((pagina < 0 || pagina >= paginaSegnalazioni.getTotalPages()) && paginaSegnalazioni.getTotalPages() > 0) {
            log.warn("Tentativo di accesso a una pagina non valida: {}. Pagine totali disponibili: {}", pagina, paginaSegnalazioni.getTotalPages());
            throw new IllegalArgumentException(messageLang.getMessage("segnalazione.invalid_page"));
        }

        log.info("Trovate {} segnalazioni nella pagina {}", paginaSegnalazioni.getNumberOfElements(), pagina);

        return paginaSegnalazioni.getContent().stream()
                .map(singolaSegnalazione -> modelMapper.map(singolaSegnalazione, SegnalazioneDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public SegnalazioneDTO prendiInCarico(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            log.error("Impossibile prendere in carico: segnalazione ID {} non trovata.", idSegnalazione);
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        if (segnalazione.getStato() != Segnalazione.StatoSegnalazione.APERTA) {
            log.warn("L'admin ID {} ha provato a prendere in carico la segnalazione ID {} che non è in stato APERTA.", idAdmin, idSegnalazione);
            throw new IllegalArgumentException(messageLang.getMessage("segnalazione.already_in_progress"));
        }

        segnalazione.setStato(Segnalazione.StatoSegnalazione.IN_LAVORAZIONE);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        log.info("Segnalazione ID {} presa in carico con successo dall'admin ID {}", idSegnalazione, idAdmin);

        return modelMapper.map(salvata, SegnalazioneDTO.class);
    }

    @Override
    @Transactional
    public SegnalazioneDTO risolviSegnalazione(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            log.error("Impossibile risolvere: segnalazione ID {} non trovata.", idSegnalazione);
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        if (segnalazione.getStato() == Segnalazione.StatoSegnalazione.CHIUSA ||
                segnalazione.getStato() == Segnalazione.StatoSegnalazione.RIFIUTATA) {
            log.warn("L'admin ID {} ha provato a risolvere la segnalazione ID {} che è già archiviata.", idAdmin, idSegnalazione);
            throw new IllegalArgumentException(messageLang.getMessage("segnalazione.already_closed"));
        }

        segnalazione.setStato(Segnalazione.StatoSegnalazione.CHIUSA);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        log.info("Segnalazione ID {} chiusa e risolta definitivamente dall'admin ID {}", idSegnalazione, idAdmin);

        return modelMapper.map(salvata, SegnalazioneDTO.class);
    }

    @Override
    @Transactional
    public SegnalazioneDTO rifiutaSegnalazione(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            log.error("Impossibile rifiutare: segnalazione ID {} non trovata.", idSegnalazione);
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        if (segnalazione.getStato() == Segnalazione.StatoSegnalazione.CHIUSA ||
                segnalazione.getStato() == Segnalazione.StatoSegnalazione.RIFIUTATA) {
            log.warn("L'admin ID {} ha provato a rifiutare la segnalazione ID {} che è già archiviata.", idAdmin, idSegnalazione);
            throw new IllegalArgumentException(messageLang.getMessage("segnalazione.already_closed"));
        }

        segnalazione.setStato(Segnalazione.StatoSegnalazione.RIFIUTATA);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        log.info("Segnalazione ID {} rifiutata dall'admin ID {}", idSegnalazione, idAdmin);

        return modelMapper.map(salvata, SegnalazioneDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public long contaSegnalazioniAperte() {
        long conteggio = segnalazioneRepository.countByStato(Segnalazione.StatoSegnalazione.APERTA);
        log.info("Conteggio effettuato: ci sono {} segnalazioni attualmente aperte", conteggio);
        return conteggio;
    }
}