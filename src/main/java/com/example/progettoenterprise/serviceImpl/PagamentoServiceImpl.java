package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Pagamento;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.repositories.PagamentoRepository;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.repositories.ViaggiatoreRepository;
import com.example.progettoenterprise.data.service.PagamentoService;
import com.example.progettoenterprise.dto.PagamentoDTO;
import com.example.progettoenterprise.dto.PrenotazioneDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }



    @Override
    @Transactional
    public PrenotazioneDTO confermaPagamento(PagamentoDTO dto, Long idUtente) {
        Prenotazione prenotazione = prenotazioneRepository.findById(dto.getIdPrenotazione())
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata."));

        if (!prenotazione.getViaggiatore().getId().equals(idUtente)) {
            throw new IllegalArgumentException("Non sei autorizzato a confermare questo pagamento.");
        }

        //Creiamo la ricevuta di pagamento
        Pagamento pagamento = new Pagamento();
        pagamento.setPrenotazione(prenotazione);
        pagamento.setImporto(dto.getImporto());

        pagamento.setRicevutaPagamento(dto.getRicevutaPagamento());
        pagamento.setTitolareCarta(dto.getTitolareCarta());
        pagamento.setStatoPagamento(Pagamento.StatoPagamento.COMPLETATO);

        pagamentoRepository.save(pagamento);

        // Aggiorniamo lo stato della prenotazione
        prenotazione.setStato(Prenotazione.StatoPrenotazione.CONFERMATA);
        Prenotazione aggiornata = prenotazioneRepository.save(prenotazione);

        return modelMapper.map(aggiornata, PrenotazioneDTO.class);
    }

    @Override
    public String creaPaymentIntent(Long idPrenotazione, Long idUtente) throws Exception {
        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata."));

        // controllo se la prenotazione è sua ed è stata pagata
        if (!prenotazione.getViaggiatore().getId().equals(idUtente)) {
            throw new IllegalArgumentException("Non sei autorizzato a pagare questa prenotazione.");
        }

        if (prenotazione.getStato() == Prenotazione.StatoPrenotazione.CONFERMATA) {
            throw new IllegalStateException("Questa prenotazione risulta già saldata.");
        }
        //  Stripe lavora in centesimi
        BigDecimal prezzoTotale = BigDecimal.valueOf(prenotazione.getViaggio().getPrezzo());
        long importoInCentesimi = prezzoTotale.multiply(new BigDecimal("100")).longValue();

        // Prepariamo i parametri per Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(importoInCentesimi)
                .setCurrency("eur")
                .putMetadata("prenotazione_id", idPrenotazione.toString())
                .build();

        // creiamo la transazione
        PaymentIntent intent = PaymentIntent.create(params);
        //il flusso di lavoro è: angular dice che l'utente tot vuole pagare tot prenotazione
        //sping calcola,usa la chiave segreta e crea un intento di pagamento
        //stripe crea la ricevuta
        //spring salva la ricevuta e manda il codice dato da stripe dal controller ad angular in jSON
        return intent.getClientSecret();
    }

    @Override
    @Transactional
    public void rimborsaPrenotazione(Long idPrenotazione) throws Exception{
        Pagamento pagamento = pagamentoRepository.findByPrenotazioneId(idPrenotazione)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento non trovato per questa prenotazione."));
        Prenotazione prenotazione = pagamento.getPrenotazione();
        // Preparo i parametri per Stripe usando il PaymentIntent ID che ho salvato quando ha pagato
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(pagamento.getRicevutaPagamento()) // qui c'è la ricevuta di pagamento
                .build();

        //Chiamo Stripe per eseguire il rimborso
        try {
            Refund refund = Refund.create(params);

            // Se va bene, aggiorniamo il DB
            pagamento.setStatoPagamento(Pagamento.StatoPagamento.RIMBORSATO);
            prenotazione.setStato(Prenotazione.StatoPrenotazione.ANNULLATA);

        } catch (StripeException e) {
            if ("charge_already_refunded".equals(e.getCode())) {
                pagamento.setStatoPagamento(Pagamento.StatoPagamento.RIMBORSATO);
                prenotazione.setStato(Prenotazione.StatoPrenotazione.ANNULLATA);
            } else {
                // Se è un errore diverso , rilanciamo l'errore
                throw e;
            }
        }
        pagamentoRepository.save(pagamento);
        prenotazioneRepository.save(prenotazione);
    }
}
