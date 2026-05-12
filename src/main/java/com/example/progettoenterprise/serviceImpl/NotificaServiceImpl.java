package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Notifica;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.NotificaRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.specifications.NotificaSpecification;
import com.example.progettoenterprise.data.service.NotificaService;
import com.example.progettoenterprise.dto.NotificaDTO;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificaServiceImpl implements NotificaService {

    private static final int SIZE_FOR_PAGE=10;

    private final UtenteRepository utenteRepository;
    private final NotificaRepository notificaRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public NotificaDTO inviaNotifica(Long utenteId, String messaggio, Long idRiferimento) {
        if (messaggio == null || messaggio.trim().isEmpty()) {
            throw new IllegalArgumentException(messageLang.getMessage("notifica.messaggio.vuoto"));
        }
        Utente destinatario=utenteRepository.findById(utenteId).orElseThrow(()->new EntityNotFoundException
                (messageLang.getMessage("utente.notexist",utenteId)));
        Notifica nuovaNotifica=new Notifica();
        nuovaNotifica.setUtente(destinatario);
        nuovaNotifica.setMessaggio(messaggio);
        nuovaNotifica.setIdRiferimento(idRiferimento);
        nuovaNotifica.setLetta(false);

        Notifica salvata = notificaRepository.save(nuovaNotifica);
        return modelMapper.map(salvata, NotificaDTO.class);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificaDTO> getNotifiche(Long utenteId, NotificaSpecification.NotificaFilter filter, int page) {

        // Controlla che l'utente esista
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> {
                    return new EntityNotFoundException(messageLang.getMessage("utente.notexist", utenteId));
                });

        // Imposta l'utente passato nel filtro
        filter.setUtenteId(utenteId);

        PageRequest pageRequest = PageRequest.of(page, SIZE_FOR_PAGE, Sort.by("id").descending());
        Page<Notifica> notifichePage=notificaRepository.findAll(NotificaSpecification.withFilter(filter),pageRequest);

        // Controllo sulla pagina corrente
        if ((page < 0 || page >= notifichePage.getTotalPages()) && notifichePage.getTotalPages() > 0) {
            throw new IllegalArgumentException(messageLang.getMessage("notifica.invalid_page"));
        }

        return notifichePage.map(n->modelMapper.map(n,NotificaDTO.class));
    }

    @Override
    @Transactional
    public void segnaComeLetta(Long id) {
        Notifica notifica=notificaRepository.findById(id).orElseThrow(()->new EntityNotFoundException
                (messageLang.getMessage("notifica.notexist",id)));
        notifica.setLetta(true);
        notificaRepository.save(notifica);

    }

    @Override
    @Transactional
    public void segnaTutteComeLette(Long utenteId) {
        List<Notifica> nonLette = notificaRepository.findAllByUtenteIdAndIsLettaIsFalseOrderByDataCreazioneDesc(utenteId);
        if (nonLette.isEmpty()) {
            return;
        }
        nonLette.forEach(n -> n.setLetta(true));
        notificaRepository.saveAll(nonLette);
    }

    @Override
    @Transactional(readOnly = true)
    public long conteggioNotificheNonLette(Long utenteId) {
        Long conteggio=notificaRepository.countByUtenteIdAndIsLettaIsFalse(utenteId);
        return conteggio;
    }

    @Override
    @Transactional
    public void eliminaNotifica(Long id) {
        if (!notificaRepository.existsById(id)) {
            throw new EntityNotFoundException(messageLang.getMessage("notifica.notexist", id));
        }
        notificaRepository.deleteById(id);

    }
}
