package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.ListaViaggioKey;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ItinerarioPreferitoRepository;
import com.example.progettoenterprise.data.repositories.ListaViaggioRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.ListaViaggioService;
import com.example.progettoenterprise.dto.ListaViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListaViaggioServiceImpl implements ListaViaggioService {
    private final ListaViaggioRepository listaViaggioRepository;
    private final ViaggioRepository viaggioRepository;
    private final ItinerarioPreferitoRepository itinerarioPreferitoRepository;
    private final MessageLang messageLang;
    private ModelMapper modelMapper;


    @Transactional
    @Override
    public ListaViaggioDTO aggiungiItinerarioAlViaggio(Long viaggioId, Long itinerarioId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException("Viaggio non trovato"));

        ItinerarioPreferito itinerarioPreferito = itinerarioPreferitoRepository.findById(itinerarioId)
                .orElseThrow(() -> new EntityNotFoundException("Itinerario non trovato"));

        // Creazione chiave e associazione
        ListaViaggioKey key = new ListaViaggioKey(itinerarioId, viaggioId);

        ListaViaggio associazione = new ListaViaggio();
        associazione.setId(key);
        associazione.setViaggio(viaggio);
        associazione.setLista(itinerarioPreferito);

        ListaViaggio salvata = listaViaggioRepository.save(associazione);
        return modelMapper.map(salvata, ListaViaggioDTO.class);
    }

    @Override
    public List<ListaViaggioDTO> getProgrammaCompleto(Long viaggioId) {
        if(!viaggioRepository.existsById(viaggioId)){
            throw new EntityNotFoundException(messageLang.getMessage("viaggio.notexist",viaggioId));

        }
        List<ListaViaggio>programma=listaViaggioRepository.findByViaggio_Id(viaggioId);
        return programma.stream().map(listaViaggio -> modelMapper.map(listaViaggio, ListaViaggioDTO.class)).toList();
    }

    @Override
    public void rimuoviItinerarioDalViaggio(Long ViaggioId,Long itinerarioId) {
        ListaViaggioKey key = new ListaViaggioKey(itinerarioId,ViaggioId);

        // Controlliamo se l'associazione esiste davvero
        if (!listaViaggioRepository.existsById(key)) {
            throw new EntityNotFoundException(
                    messageLang.getMessage("lista.notexist", ViaggioId + " - " + itinerarioId));
        }

        // Cancelliamo il record tramite la chiave composta
        listaViaggioRepository.deleteById(key);

    }

    @Override
    @Transactional
    public List<ListaViaggioDTO> cercaItinerariSottoBudget(Double Budget) {
        List<ListaViaggio> associazioniEconomiche = listaViaggioRepository.findByViaggioPrezzoLessThan(Budget);

        // se non trovo nulla, posso lanciare un avviso o restituire lista vuota
        if (associazioniEconomiche.isEmpty()) {
            return List.of();
        }

        //  Trasformazione in DTO
        // Restituiamo ListaViaggioDTO perché così il frontend ha sia le info dell'itinerario
        return associazioniEconomiche.stream()
                .map(assoc -> modelMapper.map(assoc, ListaViaggioDTO.class))
                .collect(Collectors.toList());
    }
}
