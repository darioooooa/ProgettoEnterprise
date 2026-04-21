package com.example.progettoenterprise.data.services;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.dto.ViaggioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViaggioServiceImpl implements ViaggioService {

    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;

    @Override
    @Transactional
    public Viaggio creaViaggio(ViaggioDTO viaggioDTO, Long organizzatoreId) {
        Utente organizzatore = utenteRepository.findById(organizzatoreId)
                .orElseThrow(() -> new RuntimeException("Organizzatore non trovato"));

        if (viaggioDTO.getDataFine().isBefore(viaggioDTO.getDataInizio())) {
            throw new IllegalArgumentException("La data di fine deve essere successiva a quella di inizio");
        }

        Viaggio viaggio = new Viaggio();
        viaggio.setTitolo(viaggioDTO.getTitolo());
        viaggio.setDescrizione(viaggioDTO.getDescrizione());
        viaggio.setDestinazione(viaggioDTO.getDestinazione());
        viaggio.setPrezzo(viaggioDTO.getPrezzo());
        viaggio.setDataInizio(viaggioDTO.getDataInizio());
        viaggio.setDataFine(viaggioDTO.getDataFine());
        viaggio.setOrganizzatore(organizzatore);

        return viaggioRepository.save(viaggio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viaggio> getViaggiPerOrganizzatore(Long organizzatoreId) {

        return viaggioRepository.findViaggioByOrganizzatoreId(organizzatoreId);
    }

    @Override
    @Transactional
    public void eliminaViaggio(Long viaggioId, Long organizzatoreId) {
        Viaggio viaggio = viaggioRepository.findById(viaggioId)
                .orElseThrow(() -> new RuntimeException("Viaggio non trovato"));

        if (!viaggio.getOrganizzatore().getId().equals(organizzatoreId)) {
            throw new IllegalArgumentException("Non sei autorizzato a cancellare questo viaggio");
        }

        viaggioRepository.delete(viaggio);
    }
}