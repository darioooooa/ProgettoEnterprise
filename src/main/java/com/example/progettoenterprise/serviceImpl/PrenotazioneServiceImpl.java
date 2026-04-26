package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.service.PrenotazioneService;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrenotazioneServiceImpl implements PrenotazioneService {
    private final ViaggioRepository viaggioRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final UtenteRepository utenteRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    public PrenotazioneDTO creaPrenotazione(Long idViaggio, Long idUtente, Integer numeroPersone) {
        Viaggio viaggioEsistente = viaggioRepository.findById(idViaggio)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("viaggio.notexist", idViaggio)));

        Utente utenteRichiedente = utenteRepository.findById(idUtente)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("utente.notexist", idUtente)));
        Prenotazione nuovaPrenotazione = new Prenotazione();
        nuovaPrenotazione.setViaggio(viaggioEsistente);
        nuovaPrenotazione.setViaggiatore(utenteRichiedente);
        nuovaPrenotazione.setNumeroPersone(numeroPersone);
        nuovaPrenotazione.setDataPrenotazione(LocalDateTime.now());
        nuovaPrenotazione.setStato(Prenotazione.StatoPrenotazione.CONFERMATA); // Stato iniziale standard

        Prenotazione prenotazioneSalvata = prenotazioneRepository.save(nuovaPrenotazione);
        return modelMapper.map(prenotazioneSalvata, PrenotazioneDTO.class);
    }

    @Override
    public void cancellaPrenotazione(Long idPrenotazione, Long idUtente) {
            Prenotazione prenotazione= prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", idPrenotazione)));
            if(!prenotazione.getViaggiatore().getId().equals(idUtente)){
                throw new IllegalArgumentException(messageLang.getMessage("prenotazione.unauthorized"));
            }
            prenotazioneRepository.delete(prenotazione);


    }

    @Override
    public PrenotazioneDTO getPrenotazioneById(Long id) {
       Prenotazione prenotazione= prenotazioneRepository.findById(id)
               .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", id)));
       return modelMapper.map(prenotazione, PrenotazioneDTO.class);


    }

    @Override
    public List<PrenotazioneDTO> getPrenotazioneperUtente(Long idUtente) {
        List<Prenotazione> prenotazioni= prenotazioneRepository.findByViaggiatoreId(idUtente);

        return prenotazioni.stream()
                .map(prenotazione -> modelMapper .map(prenotazione,PrenotazioneDTO.class))
                .collect(Collectors.toList());




    }


}
