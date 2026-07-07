package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ItinerarioPreferitoService {
    ItinerarioPreferitoDTO creaLista(ItinerarioPreferitoDTO dto, Long proprietarioId);
    ItinerarioPreferitoDTO getListaById(Long id);
    List<ItinerarioPreferitoDTO> getMieListe(Long proprietarioId);
    List<ItinerarioPreferitoDTO> getListeCondiviseConMe(Long utenteId);
    List<ItinerarioPreferitoDTO> cercaListePubbliche(String nome);
    void aggiungiViaggioAllaLista(Long idLista, Long idViaggio, Long idUtente);
    void rimuoviViaggioDallaLista(Long idLista, Long idViaggio, Long idUtente);
    void eliminaLista(Long id, Long utenteId);
    ItinerarioPreferitoDTO cambiaVisibilita(Long id, String visibilita, Long utenteId);
    void invitaCollaboratore(Long itinerarioId, String emailInvitato, Long idProprietario);
    void accettaInvito(Long itinerarioId, Long idUtenteInvitato);
    void rifiutaInvito(Long itinerarioId, Long idUtenteInvitato);
    List<Map<String, Object>> getInvitiInSospeso(Long utenteId);
    void spostaViaggioTraItinerari(Long idSorgente, Long idDestinazione, Long idViaggio, Long idUtente);

    @Transactional(readOnly = true)
    List<ItinerarioPreferitoDTO> getListePubblicheDiUtente(String username);
}