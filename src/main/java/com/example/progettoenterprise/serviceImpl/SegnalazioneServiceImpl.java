package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Segnalazione;
import com.example.progettoenterprise.data.repositories.ItinerarioPreferitoRepository;
import com.example.progettoenterprise.data.repositories.SegnalazioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
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

    private final UtenteRepository utenteRepository;
    private final ViaggioRepository viaggioRepository;
    private final ItinerarioPreferitoRepository itinerarioPreferitoRepository;

    @Override
    @Transactional
    public SegnalazioneDTO creaSegnalazione(SegnalazioneDTO segnalazioneDTO, Long idSegnalatore) {
        Segnalazione segnalazione = modelMapper.map(segnalazioneDTO, Segnalazione.class);
        segnalazione.setStato(Segnalazione.StatoSegnalazione.APERTA);
        segnalazione.setSegnalatoreId(idSegnalatore);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        log.info("Nuova segnalazione creata con ID: {}", salvata.getId());

        return convertiConNomi(salvata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SegnalazioneDTO> cercaSegnalazioni(SegnalazioneSpecification.SegnalazioneFilter filtro, int pagina) {
        PageRequest richiestaPagina = PageRequest.of(pagina, DIMENSIONE_PAGINA, Sort.by("dataSegnalazione").ascending());
        Page<Segnalazione> paginaSegnalazioni = segnalazioneRepository.findAll(SegnalazioneSpecification.withFilter(filtro), richiestaPagina);

        if ((pagina < 0 || pagina >= paginaSegnalazioni.getTotalPages()) && paginaSegnalazioni.getTotalPages() > 0) {
            log.warn("Tentativo di accesso a una pagina non valida: {}", pagina);
            throw new IllegalArgumentException(messageLang.getMessage("segnalazione.invalid_page"));
        }

        return paginaSegnalazioni.getContent().stream()
                .map(this::convertiConNomi)
                .toList();
    }

    @Override
    @Transactional
    public SegnalazioneDTO prendiInCarico(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        segnalazione.setStato(Segnalazione.StatoSegnalazione.IN_LAVORAZIONE);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        return convertiConNomi(salvata);
    }

    @Override
    @Transactional
    public SegnalazioneDTO risolviSegnalazione(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        segnalazione.setStato(Segnalazione.StatoSegnalazione.CHIUSA);
        segnalazione.setAdminId(idAdmin);

        if (segnalazione.getTipo() != null && segnalazione.getIdRiferimento() != null) {
            if (segnalazione.getTipo() == Segnalazione.TipoEntita.UTENTE) {
                utenteRepository.findById(segnalazione.getIdRiferimento()).ifPresent(utente -> {
                    utente.setAttivo(false);
                    utente.setMotivoSospensione(segnalazione.getMotivo());
                    utenteRepository.save(utente);
                });
            } else if (segnalazione.getTipo() == Segnalazione.TipoEntita.VIAGGIO) {
                viaggioRepository.findById(segnalazione.getIdRiferimento()).ifPresent(viaggio -> {
                    viaggioRepository.delete(viaggio);
                });
            }
        }

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        return convertiConNomi(salvata);
    }

    @Override
    @Transactional
    public SegnalazioneDTO rifiutaSegnalazione(Long idSegnalazione, Long idAdmin) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione).orElseThrow(() -> {
            return new EntityNotFoundException(messageLang.getMessage("segnalazione.notexist", idSegnalazione));
        });

        segnalazione.setStato(Segnalazione.StatoSegnalazione.RIFIUTATA);
        segnalazione.setAdminId(idAdmin);

        Segnalazione salvata = segnalazioneRepository.save(segnalazione);
        return convertiConNomi(salvata);
    }

    @Override
    @Transactional(readOnly = true)
    public long contaSegnalazioniAperte() {
        return segnalazioneRepository.countByStato(Segnalazione.StatoSegnalazione.APERTA);
    }

    private SegnalazioneDTO convertiConNomi(Segnalazione segnalazione) {
        SegnalazioneDTO dto = modelMapper.map(segnalazione, SegnalazioneDTO.class);

        if (segnalazione.getSegnalatoreId() != null) {
            utenteRepository.findById(segnalazione.getSegnalatoreId())
                    .ifPresent(utente -> dto.setSegnalatoreUsername(utente.getUsername()));
        }

        if (segnalazione.getAdminId() != null) {
            utenteRepository.findById(segnalazione.getAdminId())
                    .ifPresent(admin -> dto.setAdminUsername(admin.getUsername()));
        }

        if (segnalazione.getTipo() != null && segnalazione.getIdRiferimento() != null) {
            switch (segnalazione.getTipo()) {
                case UTENTE:
                    utenteRepository.findById(segnalazione.getIdRiferimento())
                            .ifPresent(u -> dto.setRiferimentoNome(u.getUsername()));
                    break;
                case VIAGGIO:
                    viaggioRepository.findById(segnalazione.getIdRiferimento())
                            .ifPresent(v -> dto.setRiferimentoNome(v.getTitolo()));
                    break;
                case ITINERARIO:
                    itinerarioPreferitoRepository.findById(segnalazione.getIdRiferimento())
                            .ifPresent(i -> dto.setRiferimentoNome(i.getNome()));
                    break;
                case RECENSIONE:
                    dto.setRiferimentoNome("Recensione #" + segnalazione.getIdRiferimento());
                    break;
            }
        }

        return dto;
    }
}