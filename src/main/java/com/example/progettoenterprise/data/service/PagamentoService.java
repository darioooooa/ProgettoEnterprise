package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.PagamentoDTO;
import com.example.progettoenterprise.dto.PrenotazioneDTO;

import java.util.List;

public interface PagamentoService {
    PagamentoDTO aggiungiCarta(PagamentoDTO pagamentoDTO);


    List<PagamentoDTO> getCarteViaggiatore(Long idViaggiatore);
    PrenotazioneDTO pagaPrenotazione(Long idPrenotazione, Long idMetodoPagamento, Long idUtente);

    void eliminaCarta(Long idPagamento);
}