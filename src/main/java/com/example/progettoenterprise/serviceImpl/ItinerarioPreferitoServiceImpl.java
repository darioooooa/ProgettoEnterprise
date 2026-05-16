package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ItinerarioPreferitoRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.ItinerarioPreferitoService;
import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItinerarioPreferitoServiceImpl implements ItinerarioPreferitoService {

    private final ItinerarioPreferitoRepository itinerarioRepository;
    private final UtenteRepository utenteRepository;
    private final ViaggioRepository viaggioRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public ItinerarioPreferitoDTO creaLista(ItinerarioPreferitoDTO dto, Long proprietarioId) {
        log.info("Creazione lista '{}' per utente {}", dto.getNome(), proprietarioId);

        Utente proprietario = utenteRepository.findById(proprietarioId)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", proprietarioId)));

        ItinerarioPreferito nuovaLista = new ItinerarioPreferito();
        nuovaLista.setNome(dto.getNome());
        nuovaLista.setVisibilita(dto.getVisibilita() != null ? dto.getVisibilita() : ItinerarioPreferito.Visibilita.PRIVATA);
        nuovaLista.setProprietario(proprietario);

        return modelMapper.map(itinerarioRepository.save(nuovaLista), ItinerarioPreferitoDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItinerarioPreferitoDTO> getMieListe(Long proprietarioId) {
        return itinerarioRepository.findByProprietarioId(proprietarioId).stream()
                .map(l -> modelMapper.map(l, ItinerarioPreferitoDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItinerarioPreferitoDTO> cercaListePubbliche(String nome) {
        return itinerarioRepository.findByNomeContainingIgnoreCaseAndVisibilita(nome, ItinerarioPreferito.Visibilita.PUBBLICA)
                .stream()
                .map(l -> modelMapper.map(l, ItinerarioPreferitoDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItinerarioPreferitoDTO cambiaVisibilita(Long id, String nuovaVisibilita, Long utenteId) {
        ItinerarioPreferito lista = itinerarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("lista.notexist", id)));

        if (!lista.getProprietario().getId().equals(utenteId)) {
            throw new IllegalArgumentException(messageLang.getMessage("lista.unauthorized"));
        }

        try {
            lista.setVisibilita(ItinerarioPreferito.Visibilita.valueOf(nuovaVisibilita.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Visibilità non valida: " + nuovaVisibilita);
        }

        return modelMapper.map(itinerarioRepository.save(lista), ItinerarioPreferitoDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ItinerarioPreferitoDTO getListaById(Long id) {
        return itinerarioRepository.findById(id)
                .map(l -> modelMapper.map(l, ItinerarioPreferitoDTO.class))
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("lista.notexist", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItinerarioPreferitoDTO> getListeCondiviseConMe(Long utenteId) {
        return itinerarioRepository.findByUtentiAutorizzati_Utente_Id(utenteId).stream()
                .map(l -> modelMapper.map(l, ItinerarioPreferitoDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminaLista(Long id, Long utenteId) {
        ItinerarioPreferito lista = itinerarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("lista.notexist", id)));

        if (!lista.getProprietario().getId().equals(utenteId)) {
            throw new IllegalArgumentException(messageLang.getMessage("lista.unauthorized"));
        }
        itinerarioRepository.delete(lista);
    }

    @Override
    @Transactional
    public void aggiungiViaggioAllaLista(Long idLista, Long idViaggio, Long idUtente) {
        ItinerarioPreferito lista = itinerarioRepository.findById(idLista)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("itinerario.notexist", idLista)));
        if (!lista.getProprietario().getId().equals(idUtente)) {
            throw new IllegalArgumentException(messageLang.getMessage("itinerario.unauthorized"));
        }
        Viaggio viaggio = viaggioRepository.findById(idViaggio)
                .orElseThrow(() -> new EntityNotFoundException("Viaggio non trovato con ID: " + idViaggio));

        boolean giaPresente = lista.getContenuti().stream()
                .anyMatch(collegamento -> collegamento.getViaggio().getId().equals(idViaggio));

        if (giaPresente) {
            throw new IllegalArgumentException("Il viaggio è già presente in questa lista.");
        }

        ListaViaggio collegamento = new ListaViaggio();
        collegamento.setLista(lista);
        collegamento.setViaggio(viaggio);

        lista.getContenuti().add(collegamento);
        itinerarioRepository.save(lista);

        log.info("Viaggio {} aggiunto alla lista {} dall'utente {}", idViaggio, idLista, idUtente);
    }

    @Override
    @Transactional
    public void rimuoviViaggioDallaLista(Long idLista, Long idViaggio, Long idUtente) {

        ItinerarioPreferito lista = itinerarioRepository.findById(idLista)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("itinerario.notexist", idLista)));

        if (!lista.getProprietario().getId().equals(idUtente)) {
            throw new IllegalArgumentException(messageLang.getMessage("itinerario.unauthorized"));
        }
        boolean rimosso = lista.getContenuti().removeIf(collegamento ->
                collegamento.getViaggio().getId().equals(idViaggio)
        );

        if (!rimosso) {
            throw new IllegalArgumentException("Il viaggio non è presente in questa lista.");
        }

        itinerarioRepository.save(lista);

        log.info("Viaggio {} rimosso dalla lista {} dall'utente {}", idViaggio, idLista, idUtente);
    }
}