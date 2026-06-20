package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.PagamentoDTO;
import com.example.progettoenterprise.dto.PrenotazioneDTO;

import java.util.List;

public interface PagamentoService {
    PrenotazioneDTO confermaPagamento(PagamentoDTO dto, Long idUtente);
    String creaPaymentIntent(Long idPrenotazione, Long idUtente) throws Exception;
    void rimborsaPrenotazione(Long idPrenotazione)throws Exception;
}