package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.RaccomandazioneService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RaccomandazioneServiceImpl implements RaccomandazioneService {

    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ViaggioDTO> getConsigliatiPerUtente(Long utenteId) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato"));

        Map<String, Integer> profiloTag = new HashMap<>();

        if (utente.getMieiItinerari() != null) {
            for (ItinerarioPreferito lista : utente.getMieiItinerari()) {
                if (lista.getContenuti() != null) {
                    for (ListaViaggio listaViaggio : lista.getContenuti()) {
                        aggiungiTagAlProfilo(listaViaggio.getViaggio(), profiloTag);
                    }
                }
            }
        }

        if (utente instanceof Viaggiatore) {
            Viaggiatore viaggiatore = (Viaggiatore) utente;
            if (viaggiatore.getMiePrenotazioni() != null) {
                for (Prenotazione prenotazione : viaggiatore.getMiePrenotazioni()) {
                    if (prenotazione.getStato() == Prenotazione.StatoPrenotazione.CONFERMATA) {
                        aggiungiTagAlProfilo(prenotazione.getViaggio(), profiloTag);
                    }
                }
            }
        }

        log.info("Profilo tag generato per utente {}: {}", utenteId, profiloTag);

        if (profiloTag.isEmpty()) {
            return Collections.emptyList();
        }

        List<Viaggio> viaggiDisponibili = viaggioRepository.findAll().stream()
                .filter(v -> v.getStato() == Viaggio.StatoViaggio.APERTO)
                .collect(Collectors.toList());

        //  Calcola il punteggio per ogni viaggio e ordina
        List<Viaggio> viaggiConsigliati = viaggiDisponibili.stream()
                .filter(v -> v.getOrganizzatore() != null && !v.getOrganizzatore().getId().equals(utenteId))
                .map(viaggio -> {
                    int punteggio = calcolaPunteggio(viaggio, profiloTag);
                    return new AbstractMap.SimpleEntry<>(viaggio, punteggio);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());

        return viaggiConsigliati.stream()
                .map(v -> modelMapper.map(v, ViaggioDTO.class))
                .collect(Collectors.toList());
    }

    private void aggiungiTagAlProfilo(Viaggio viaggio, Map<String, Integer> profilo) {
        if (viaggio != null && viaggio.getTags() != null) {
            for (Tag tag : viaggio.getTags()) {
                profilo.put(tag.getNomeTag(), profilo.getOrDefault(tag.getNomeTag(), 0) + 1);
            }
        }
    }

    private int calcolaPunteggio(Viaggio viaggio, Map<String, Integer> profilo) {
        int punteggio = 0;
        if (viaggio.getTags() != null) {
            for (Tag tag : viaggio.getTags()) {
                punteggio += profilo.getOrDefault(tag.getNomeTag(), 0);
            }
        }
        return punteggio;
    }
}
