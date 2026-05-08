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

import java.nio.charset.StandardCharsets;
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

    private String formattaDataIcs(LocalDateTime data) {
        if (data == null) return "";


        return data.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    }


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
        nuovaPrenotazione.setStato(Prenotazione.StatoPrenotazione.IN_ATTESA); // Stato iniziale standard

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
    @Override
    public byte[] esportaPrenotazioni(Long idPrenotazione) {
        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new EntityNotFoundException(messageLang.getMessage("prenotazione.notexist", idPrenotazione)));

        Viaggio viaggio = prenotazione.getViaggio();

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\n")
                .append("VERSION:2.0\n")
                .append("PRODID:-//ProgettoEnterprise//GestioneViaggi//IT\n")
                .append("BEGIN:VEVENT\n")
                .append("UID:").append(prenotazione.getId()).append("@gestione-viaggi.it\n")
                .append("SUMMARY:").append(viaggio.getTitolo()).append("\n")
                .append("DESCRIPTION:Prenotazione per ").append(prenotazione.getNumeroPersone()).append(" persone\n")

                // Ora passiamo LocalDateTime, e il metodo lo gestirà correttamente
                .append("DTSTART:").append(formattaDataIcs(viaggio.getDataInizio())).append("\n")
                .append("DTEND:").append(formattaDataIcs(viaggio.getDataFine())).append("\n")

                .append("END:VEVENT\n")
                .append("END:VCALENDAR");

        return ics.toString().getBytes(StandardCharsets.UTF_8);
    }


}
