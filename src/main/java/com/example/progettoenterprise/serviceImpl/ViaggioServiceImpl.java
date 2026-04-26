package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.dto.ViaggioDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViaggioServiceImpl implements ViaggioService {

    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    @Transactional
    public ViaggioDTO creaViaggio(ViaggioDTO viaggioDTO, String organizzatoreUsername) {
        Utente organizzatore = utenteRepository.findByUsername(organizzatoreUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("utente.notfound", organizzatoreUsername)));
        Viaggio viaggio = modelMapper.map(viaggioDTO, Viaggio.class);

        // Controllo sui dati del viaggio
        if (viaggio.getPrezzo() <= 0) {
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_price"));
        }
        if (viaggio.getDataFine().isBefore(viaggio.getDataInizio()) || viaggio.getDataInizio() == null){
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.invalid_date"));
        }

        viaggio.setOrganizzatore(organizzatore);
        viaggio.setMediaRecensioni(0.0);
        viaggio.setNumeroRecensioni(0);
        Viaggio salvato = viaggioRepository.save(viaggio);
        return modelMapper.map(salvato, ViaggioDTO.class);
    }

    @Override
    public List<ViaggioDTO> getViaggiPerOrganizzatore(Long organizzatoreId) {
        return viaggioRepository.findViaggioByOrganizzatoreId(organizzatoreId)
                .stream()
                .map(v -> modelMapper.map(v, ViaggioDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminaViaggio(Long viaggioId, Long organizzatoreId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", viaggioId)));
        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)) {
            throw new IllegalArgumentException(messageLang.getMessage("viaggio.unauthorized"));
        }

        viaggioRepository.delete(viaggio);
    }

    // Metodo per fornire dettagli delle recensioni di un viaggio
    @Override
    @Transactional(readOnly = true)
    // Prima cerca le statistiche del viaggio nella cache
    @Cacheable(value = CacheConfig.CACHE_VIAGGI_MEDIA, key = "#viaggioId")
    public Map<String, Object> getStatisticheRecensioni(Long viaggioId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageLang.getMessage("viaggio.notexist", viaggioId)));

        return Map.of(
                "viaggioId", viaggio.getId(),
                "mediaRecensioni", viaggio.getMediaRecensioni(),
                "numeroRecensioni", viaggio.getNumeroRecensioni()
        );
    }
}