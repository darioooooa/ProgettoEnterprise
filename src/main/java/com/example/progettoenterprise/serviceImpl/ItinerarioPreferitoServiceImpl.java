package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.ItinerarioPreferitoRepository;
import com.example.progettoenterprise.data.repositories.ListaUtenteRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.ItinerarioPreferitoService;
import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final ListaUtenteRepository listaUtenteRepository;

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
        List<ItinerarioPreferito> liste = itinerarioRepository.findByProprietarioId(proprietarioId);

        return liste.stream().map(lista -> {
            ItinerarioPreferitoDTO dto = modelMapper.map(lista, ItinerarioPreferitoDTO.class);

            boolean condiviso = listaUtenteRepository.existsByListaIdAndStato(lista.getId(), ListaUtente.StatoInvito.ACCETTATO);
            dto.setInCondivisione(condiviso);

            return dto;
        }).collect(Collectors.toList());
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
    @Transactional
    public void invitaCollaboratore(Long itinerarioId, String emailInvitato, Long idProprietario) {
        ItinerarioPreferito itinerario = itinerarioRepository.findById(itinerarioId)
                .orElseThrow(() -> new EntityNotFoundException("Itinerario non trovato"));

        if (!itinerario.getProprietario().getId().equals(idProprietario)) {
            throw new AccessDeniedException("Solo il proprietario può invitare amici.");
        }

        Utente utenteInvitato = utenteRepository.findByEmail(emailInvitato)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con email: " + emailInvitato));

        // Controlla che non stia invitando se stesso
        if (utenteInvitato.getId().equals(idProprietario)) {
            throw new IllegalArgumentException("Non puoi invitare te stesso!");
        }

        // Evita doppioni
        boolean giaPresente = itinerario.getUtentiAutorizzati().stream()
                .anyMatch(lu -> lu.getUtente().getId().equals(utenteInvitato.getId()));
        if (giaPresente) {
            throw new IllegalStateException("Utente già invitato o già presente.");
        }

        // Crea il biglietto d'invito
        ListaUtente invito = new ListaUtente();
        invito.setId(new ListaUtenteKey(utenteInvitato.getId(), itinerario.getId()));
        invito.setUtente(utenteInvitato);
        invito.setLista(itinerario);
        invito.setStato(ListaUtente.StatoInvito.IN_ATTESA);

        itinerario.getUtentiAutorizzati().add(invito);
        itinerarioRepository.save(itinerario);
    }

    @Override
    public void accettaInvito(Long itinerarioId, Long idUtenteInvitato) {
        ListaUtenteKey key = new ListaUtenteKey(idUtenteInvitato, itinerarioId);

        ListaUtente invito = listaUtenteRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException("Invito non trovato."));

        invito.setStato(ListaUtente.StatoInvito.ACCETTATO);
        listaUtenteRepository.save(invito);

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
        List<ListaUtente> condivise = listaUtenteRepository.findByUtenteIdAndStato(utenteId, ListaUtente.StatoInvito.ACCETTATO);

        return condivise.stream().map(listaUtente -> {
            ItinerarioPreferito lista = listaUtente.getLista();
            ItinerarioPreferitoDTO dto = modelMapper.map(lista, ItinerarioPreferitoDTO.class);

            if (lista.getProprietario() != null) {
                dto.setProprietarioUsername(lista.getProprietario().getUsername());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInvitiInSospeso(Long utenteId) {
        // Peschiamo solo gli inviti in attesa
        List<ListaUtente> inviti = listaUtenteRepository.findByUtenteIdAndStato(utenteId, ListaUtente.StatoInvito.IN_ATTESA);

        return inviti.stream().map(invito -> Map.<String, Object>of(
                "idItinerario", invito.getLista().getId(),
                "nomeItinerario", invito.getLista().getNome(),
                "proprietario", invito.getLista().getProprietario().getUsername(),
                "emailProprietario", invito.getLista().getProprietario().getEmail()
        )).collect(Collectors.toList());
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

        //controllo nuovo che serve per condividere l'itinerario
        boolean isProprietario = lista.getProprietario().getId().equals(idUtente);

        boolean isCollaboratoreAutorizzato = lista.getUtentiAutorizzati().stream()
                .anyMatch(collaboratore ->
                        collaboratore.getUtente().getId().equals(idUtente) &&
                                collaboratore.getStato() == ListaUtente.StatoInvito.ACCETTATO);

        if (!isProprietario && !isCollaboratoreAutorizzato) {
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

        boolean isProprietario = lista.getProprietario().getId().equals(idUtente);

        boolean isCollaboratoreAutorizzato = lista.getUtentiAutorizzati().stream()
                .anyMatch(collaboratore ->
                        collaboratore.getUtente().getId().equals(idUtente) &&
                                collaboratore.getStato() == ListaUtente.StatoInvito.ACCETTATO);

        if (!isProprietario && !isCollaboratoreAutorizzato) {
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

    @Transactional(readOnly = true)
    @Override
    public List<ItinerarioPreferitoDTO> getListePubblicheDiUtente(String username) {
        return itinerarioRepository.findByProprietarioUsernameAndVisibilita(username, ItinerarioPreferito.Visibilita.PUBBLICA)
                .stream()
                .map(l -> modelMapper.map(l, ItinerarioPreferitoDTO.class))
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void rifiutaInvito(Long itinerarioId, Long idUtenteInvitato) {
        ListaUtenteKey key = new ListaUtenteKey(idUtenteInvitato, itinerarioId);

        ListaUtente invito = listaUtenteRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException("Invito non trovato."));

        // Eliminiamo l'invito per fare pulizia nel database
        listaUtenteRepository.delete(invito);
        log.info("Invito per l'itinerario {} rifiutato ed eliminato dall'utente {}", itinerarioId, idUtenteInvitato);
    }
}