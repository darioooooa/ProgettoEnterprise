package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.repositories.specifications.NotificaSpecification;
import com.example.progettoenterprise.dto.NotificaDTO;
import org.springframework.data.domain.Page;

public interface NotificaService {
    NotificaDTO inviaNotifica(Long utenteId, String messaggio, Long idRiferimento);
    Page<NotificaDTO> getNotifiche(Long utenteId, NotificaSpecification.NotificaFilter filter, int page);
    void eliminaNotifica(Long id);

    void segnaComeLetta(Long id);
    void segnaTutteComeLette(Long utenteId);
    long conteggioNotificheNonLette(Long utenteId);
}
