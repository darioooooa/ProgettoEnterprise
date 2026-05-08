package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Pagamento;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.PagamentoRepository;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.ViaggiatoreRepository;
import com.example.progettoenterprise.data.service.PagamentoService;
import com.example.progettoenterprise.dto.PagamentoDTO;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagamentoServiceImpl implements PagamentoService {
        private final PagamentoRepository pagamentoRepository;
        private final ViaggiatoreRepository viaggiatoreRepository;
        private final PrenotazioneRepository prenotazioneRepository;
        private final ModelMapper modelMapper;
        private final MessageLang messageLang;

    @Override
    public PagamentoDTO aggiungiCarta(PagamentoDTO pagamentoDTO) {
        Viaggiatore viaggiatore= viaggiatoreRepository.findById(pagamentoDTO.getIdViaggiatore())
                .orElseThrow(() -> new IllegalArgumentException(messageLang.getMessage("viaggiatore.notfound",pagamentoDTO.getIdViaggiatore())));
        Pagamento pagamento=modelMapper.map(pagamentoDTO, Pagamento.class);
        pagamento.setViaggiatore(viaggiatore);
        Pagamento salvato= pagamentoRepository.save(pagamento);

        return modelMapper.map(salvato, PagamentoDTO.class);
    }

    @Override
    public List<PagamentoDTO> getCarteViaggiatore(Long idViaggiatore) {

        return pagamentoRepository.findByViaggiatoreId(idViaggiatore)
                .stream()
                .map(p -> {
                    PagamentoDTO dto = modelMapper.map(p, PagamentoDTO.class);
                    //  oscuriamo il numero prima di mandarlo al frontend
                    String numeroSemplice = p.getNumeroCarta();
                    if(numeroSemplice != null && numeroSemplice.length() >= 4) {
                        dto.setNumeroOscurato("**** **** **** " + numeroSemplice.substring(numeroSemplice.length() - 4));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public PrenotazioneDTO pagaPrenotazione(Long idPrenotazione, Long idMetodoPagamento, Long idUtente) {

        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata."));

        if (!prenotazione.getViaggiatore().getId().equals(idUtente)) {
            throw new IllegalArgumentException("Non sei autorizzato a saldare questo conto.");
        }

        if (prenotazione.getStato() == Prenotazione.StatoPrenotazione.CONFERMATA) {
            throw new IllegalStateException("Questo viaggio risulta già saldato.");
        }


        Pagamento carta = pagamentoRepository.findById(idMetodoPagamento)
                .orElseThrow(() -> new EntityNotFoundException("Metodo di pagamento non trovato."));

        if (!carta.getViaggiatore().getId().equals(idUtente)) {
            throw new IllegalArgumentException("Questa carta non appartiene al tuo profilo.");
        }

        // QUI POI USEREMO STRIPE PER SIMULARE LA TRANSAZIONE.
        prenotazione.setStato(Prenotazione.StatoPrenotazione.CONFERMATA);
        Prenotazione aggiornata = prenotazioneRepository.save(prenotazione);
        return modelMapper.map(aggiornata, PrenotazioneDTO.class);
    }

    @Override
    @Transactional
    public void eliminaCarta(Long idPagamento) {

        if (!pagamentoRepository.existsById(idPagamento)) {
            throw new EntityNotFoundException("Carta non trovata con ID: " + idPagamento);
        }
        pagamentoRepository.deleteById(idPagamento);
    }
}
