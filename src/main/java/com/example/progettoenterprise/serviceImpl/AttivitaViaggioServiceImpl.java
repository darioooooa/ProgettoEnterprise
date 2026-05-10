package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.AttivitaViaggioRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.specifications.AttivitaViaggioSpecification;
import com.example.progettoenterprise.data.service.AttivitaViaggioService;
import com.example.progettoenterprise.dto.AttivitaViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttivitaViaggioServiceImpl implements AttivitaViaggioService {
    private final AttivitaViaggioRepository attivitaViaggioRepository;
    private final ViaggioRepository viaggioRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;
    private final UtenteRepository utenteRepository;

    @Override
    @Transactional
    public AttivitaViaggioDTO creaAttivita(Long viaggioId, AttivitaViaggioDTO attivitaViaggioDTO, Long organizzatoreId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId)));

        // Controlla se l'utente loggato è il proprietario del viaggio
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)) {
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }

        AttivitaViaggio entity = modelMapper.map(attivitaViaggioDTO, AttivitaViaggio.class);

        entity.setViaggio(viaggio);
        AttivitaViaggio salvato = attivitaViaggioRepository.save(entity);
        return modelMapper.map(salvato,AttivitaViaggioDTO.class);

    }

    @Override
    public AttivitaViaggioDTO getAttivitaById(Long id, Long viaggioId, Long utenteId) {
            AttivitaViaggio attivita=attivitaViaggioRepository.findById(id).orElseThrow(()->
                    new EntityNotFoundException(messageLang.getMessage("attivita.notexist",id)));

            Utente utente = utenteRepository.findById(utenteId)
                    .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId)));

            // Controlla che l'attività appartenga al viaggio specificato
            if (!attivita.getViaggio().getId().equals(viaggioId)){
                throw new EntityNotFoundException(messageLang.getMessage("attivita.notexist", id));
            }

            // Controlla, se è l'organizzatore del viaggio, che possa vedere solo le sue attività
            if(utente.getRuolo().equals(Utente.Ruolo.ROLE_ORGANIZZATORE)){
                if(!attivita.getViaggio().getOrganizzatore().getId().equals(utenteId)){
                    throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
                }
            }
            return modelMapper.map(attivita,AttivitaViaggioDTO.class);
    }

    @Override
    @Transactional
    public AttivitaViaggioDTO modificaAttivitaViaggio(Long id,AttivitaViaggioDTO dto, Long organizzatoreId) {
        // Cerca l'attività esistente. Se non c'è, lancia l'errore
        return attivitaViaggioRepository.findById(id).map(entityEsistente -> {

            if (!entityEsistente.getViaggio().getOrganizzatore().getId().equals(organizzatoreId)) {
                throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
            }

            // Aggiorna i campi della Entity con i dati che arrivano dal DTO
            entityEsistente.setTitolo(dto.getTitolo());
            entityEsistente.setDescrizione(dto.getDescrizione());
            entityEsistente.setCosto(dto.getCosto());
            entityEsistente.setOrarioInizio(dto.getOrarioInizio());
            entityEsistente.setOrarioFine(dto.getOrarioFine());
            entityEsistente.setPosizione(dto.getPosizione());

            // Salva le modifiche nel database
            AttivitaViaggio salvata = attivitaViaggioRepository.save(entityEsistente);

            // Trasforma l'entità aggiornata in DTO e la restituisce
            return modelMapper.map(salvata, AttivitaViaggioDTO.class);

        }).orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("attivita.notexist", id)));
    }

    @Override
    @Transactional
    public void eliminaAttivitaViaggio(Long idAttivita,Long idViaggio, Long organizzatoreId) {
        AttivitaViaggio attivita = attivitaViaggioRepository.findById(idAttivita)
                .orElseThrow(() -> new EntityNotFoundException("Attività non trovata con ID: " + idAttivita));

        // Controllo se l'attività appartiene al viaggio specificato
        if (!attivita.getViaggio().getId().equals(idViaggio)) {
            throw new IllegalArgumentException("Errore: l'attività non appartiene a questo viaggio!");
        }

        // Controlla che l'organizzatore del viaggio sia l'utente loggato
        if (!attivita.getViaggio().getOrganizzatore().getId().equals(organizzatoreId)) {
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }

        // Eliminazione
        attivitaViaggioRepository.delete(attivita);

    }

    @Override
    public List<AttivitaViaggioDTO> ricercaFiltrata(AttivitaViaggioSpecification.AttivitaFilter attivitaFilter, Long viaggioId, Long utenteId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", viaggioId)));

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId)));

        // Se è un organizzatore, deve essere l'organizzatore del viaggio per vederne le attività
        if (utente.getRuolo().equals(Utente.Ruolo.ROLE_ORGANIZZATORE)){
            if (!viaggio.getOrganizzatore().getId().equals(utenteId)){
                throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
            }
        }

        attivitaFilter.setViaggioId(viaggioId);
        List<AttivitaViaggio> risultati = attivitaViaggioRepository.findAll(AttivitaViaggioSpecification.withFilter(attivitaFilter));

        return risultati.stream()
                .map(entity -> modelMapper.map(entity, AttivitaViaggioDTO.class))
                .collect(Collectors.toList());
    }


}
