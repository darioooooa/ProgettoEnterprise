package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.PagamentoDTO;
import java.util.List;

public interface PagamentoService {
    PagamentoDTO aggiungiCarta(PagamentoDTO pagamentoDTO);


    List<PagamentoDTO> getCarteViaggiatore(Long idViaggiatore);


    void eliminaCarta(Long idPagamento);
}