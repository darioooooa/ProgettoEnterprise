package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.NotificaDTO;

import java.util.List;

public interface NotificaService {
    NotificaDTO inviaNotifica(Long utenteId, String messaggio, Long idRiferimento);

    List<NotificaDTO> getNotifiche(Long utenteId);
    List<NotificaDTO> getNotificheNonLette(Long utenteId);

    void segnaComeLetta(Long id);
    void segnaTutteComeLette(Long utenteId);
    long conteggioNotificheNonLette(Long utenteId);
    void eliminaNotifica(Long id);
}
