package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.AttivitaViaggio;
import com.example.progettoenterprise.data.repositories.AttivitaViaggioRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
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

    @Override
    @Transactional
    public AttivitaViaggioDTO creaAttivita(AttivitaViaggioDTO attivitaViaggioDTO) {
        AttivitaViaggio entity = modelMapper.map(attivitaViaggioDTO, AttivitaViaggio.class);
        AttivitaViaggio salvato = attivitaViaggioRepository.save(entity);
        return modelMapper.map(salvato,AttivitaViaggioDTO.class);

    }

    @Override
    public AttivitaViaggioDTO getAttivitaById(Long id) {
            AttivitaViaggio attivita=attivitaViaggioRepository.findById(id).orElseThrow(()->
                    new EntityNotFoundException(messageLang.getMessage("attivita.notexist",id)));
            return modelMapper.map(attivita,AttivitaViaggioDTO.class);
    }

    @Override
    public List<AttivitaViaggioDTO> getTimelineSpostamenti(Long viaggioId) {
        return attivitaViaggioRepository.findByViaggioIdOrderByOrarioInizioAsc(viaggioId)
                .stream()
                .map(entity -> modelMapper.map(entity, AttivitaViaggioDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AttivitaViaggioDTO> cercaInViaggio(Long viaggioId, String keyword) {
        return attivitaViaggioRepository.findByViaggioIdAndTitoloContainingIgnoreCase(viaggioId,keyword)
                .stream()
                .map(entity -> modelMapper.map(entity, AttivitaViaggioDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AttivitaViaggioDTO> filtraPerBudget(Long viaggioId, Double budgetMax) {
        return attivitaViaggioRepository.findByViaggioIdAndCostoLessThanEqual(viaggioId,budgetMax)
                .stream()
                .map(entity -> modelMapper.map(entity, AttivitaViaggioDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public AttivitaViaggioDTO modificaAttivitaViaggio(Long id,AttivitaViaggioDTO dto) {
        // 1. Cerchiamo l'attività esistente. Se non c'è, lanciamo l'errore (come ha fatto il tuo compagno)
        return attivitaViaggioRepository.findById(id).map(entityEsistente -> {

            // 2. Aggiorniamo i campi della Entity con i dati che arrivano dal DTO
            // Puoi farlo a mano come il tuo compagno
            entityEsistente.setTitolo(dto.getTitolo());
            entityEsistente.setDescrizione(dto.getDescrizione());
            entityEsistente.setCosto(dto.getCosto());
            entityEsistente.setOrarioInizio(dto.getOrarioInizio());
            entityEsistente.setOrarioFine(dto.getOrarioFine());





            // 3. Salviamo le modifiche nel database
            AttivitaViaggio salvata = attivitaViaggioRepository.save(entityEsistente);

            // 4. Trasformiamo l'entità aggiornata in DTO e la restituiamo
            return modelMapper.map(salvata, AttivitaViaggioDTO.class);

        }).orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("attivita.notexist", id)));
    }

    @Override
    @Transactional
    public void eliminaAttivitaViaggio(Long idAttivita,Long idViaggio) {
        AttivitaViaggio attivita = attivitaViaggioRepository.findById(idAttivita)
                .orElseThrow(() -> new RuntimeException("Attività non trovata con ID: " + idAttivita));

        // 2. Controllo di sicurezza: l'attività appartiene al viaggio specificato?
        if (!attivita.getViaggio().getId().equals(idViaggio)) {
            throw new RuntimeException("Errore: l'attività non appartiene a questo viaggio!");
        }

        // 3. Eliminazione
        attivitaViaggioRepository.delete(attivita);

    }


}
