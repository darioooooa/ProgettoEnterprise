package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.*;
import com.example.progettoenterprise.dto.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceImplTest {

    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private PrenotazioneRepository prenotazioneRepository;
    @Mock private ViaggiatoreRepository viaggiatoreRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private PagamentoServiceImpl pagamentoService;
    private Prenotazione prenotazioneValida;
    private Utente utenteViaggiatore;
    private Viaggio viaggioAssociato;
    private PagamentoDTO pagamentoDTO;
    private Pagamento pagamentoValido;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagamentoService, "stripeApiKey", "sk_test_fake_key_123");
        utenteViaggiatore = mock(Utente.class);
        lenient().when(utenteViaggiatore.getId()).thenReturn(1L);
        lenient().when(utenteViaggiatore.getFirebaseToken()).thenReturn("token-viaggiatore");
        lenient().when(utenteViaggiatore.getUsername()).thenReturn("mario88");

        Utente organizzatore = mock(Utente.class);
        lenient().when(organizzatore.getFirebaseToken()).thenReturn("token-organizzatore");

        viaggioAssociato = new Viaggio();
        viaggioAssociato.setId(10L);
        viaggioAssociato.setPrezzo(500.0);
        viaggioAssociato.setOrganizzatore(organizzatore);
        viaggioAssociato.setDestinazione("Roma");

        prenotazioneValida = new Prenotazione();
        prenotazioneValida.setId(100L);
        prenotazioneValida.setViaggiatore(utenteViaggiatore);
        prenotazioneValida.setViaggio(viaggioAssociato);
        prenotazioneValida.setStato(Prenotazione.StatoPrenotazione.IN_ATTESA);

        pagamentoDTO = new PagamentoDTO();
        pagamentoDTO.setIdPrenotazione(100L);
        pagamentoDTO.setImporto(new BigDecimal("500.00"));
        pagamentoDTO.setRicevutaPagamento("pi_12345");
        pagamentoDTO.setTitolareCarta("Mario Rossi");

        pagamentoValido = new Pagamento();
        pagamentoValido.setId(50L);
        pagamentoValido.setPrenotazione(prenotazioneValida);
        pagamentoValido.setRicevutaPagamento("pi_12345");
        pagamentoValido.setStatoPagamento(Pagamento.StatoPagamento.COMPLETATO);
    }

    @Test
    @DisplayName("Init: Imposta correttamente la chiave API di Stripe")
    void testInitStripeKey() {
        pagamentoService.init();
        assertEquals("sk_test_fake_key_123", Stripe.apiKey);
    }


    @Test
    @DisplayName("Conferma Pagamento: Elaborazione corretta e salvataggio a DB")
    void testConfermaPagamentoSuccesso() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneValida));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArguments()[0]);
        when(prenotazioneRepository.save(any(Prenotazione.class))).thenAnswer(i -> i.getArguments()[0]);

        PrenotazioneDTO expectedDto = new PrenotazioneDTO();
        when(modelMapper.map(any(), eq(PrenotazioneDTO.class))).thenReturn(expectedDto);

        PrenotazioneDTO result = pagamentoService.confermaPagamento(pagamentoDTO, 1L);

        assertNotNull(result);
        verify(pagamentoRepository).save(any(Pagamento.class));
        verify(prenotazioneRepository).save(prenotazioneValida);
        assertEquals(Prenotazione.StatoPrenotazione.CONFERMATA, prenotazioneValida.getStato());
    }

    @Test
    @DisplayName("Conferma Pagamento: Errore se la prenotazione non esiste")
    void testConfermaPagamentoPrenotazioneNonTrovata() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> pagamentoService.confermaPagamento(pagamentoDTO, 1L));
    }

    @Test
    @DisplayName("Conferma Pagamento: Errore se l'utente non è il proprietario della prenotazione")
    void testConfermaPagamentoUtenteNonAutorizzato() {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneValida));

        assertThrows(IllegalArgumentException.class, () -> pagamentoService.confermaPagamento(pagamentoDTO, 999L));
    }


    @Test
    @DisplayName("Crea PaymentIntent: Restituisce correttamente il secret di Stripe")
    void testCreaPaymentIntentSuccesso() throws Exception {
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneValida));

        try (MockedStatic<PaymentIntent> mockedStripe = mockStatic(PaymentIntent.class)) {
            PaymentIntent fintoIntent = mock(PaymentIntent.class);
            when(fintoIntent.getClientSecret()).thenReturn("segreto_stripe_123");

            mockedStripe.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class))).thenReturn(fintoIntent);

            String codiceSegreto = pagamentoService.creaPaymentIntent(100L, 1L);

            assertEquals("segreto_stripe_123", codiceSegreto);
            mockedStripe.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)));
        }
    }

    @Test
    @DisplayName("Crea PaymentIntent: Errore se la prenotazione è già stata pagata")
    void testCreaPaymentIntentGiaPagata() {
        prenotazioneValida.setStato(Prenotazione.StatoPrenotazione.CONFERMATA);
        when(prenotazioneRepository.findById(100L)).thenReturn(Optional.of(prenotazioneValida));

        assertThrows(IllegalStateException.class, () -> pagamentoService.creaPaymentIntent(100L, 1L));
    }

    @Test
    @DisplayName("Rimborsa Prenotazione caso successo")
    void testRimborsaPrenotazioneSuccesso() throws Exception {
        when(pagamentoRepository.findByPrenotazioneId(100L)).thenReturn(Optional.of(pagamentoValido));

        try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
            Refund fintoRimborso = mock(Refund.class);
            mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class))).thenReturn(fintoRimborso);

            pagamentoService.rimborsaPrenotazione(100L);

            assertEquals(Pagamento.StatoPagamento.RIMBORSATO, pagamentoValido.getStatoPagamento());
            assertEquals(Prenotazione.StatoPrenotazione.ANNULLATA, prenotazioneValida.getStato());

            verify(pagamentoRepository).save(pagamentoValido);
            verify(prenotazioneRepository).save(prenotazioneValida);
        }
    }

    @Test
    @DisplayName("Rimborsa Prenotazione se il rimborso è già avvenuto su Stripe")
    void testRimborsaPrenotazioneGiaRimborsata() throws Exception {
        when(pagamentoRepository.findByPrenotazioneId(100L)).thenReturn(Optional.of(pagamentoValido));

        try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
            StripeException fintaEccezione = mock(StripeException.class);
            when(fintaEccezione.getCode()).thenReturn("charge_already_refunded");

            mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class))).thenThrow(fintaEccezione);

            pagamentoService.rimborsaPrenotazione(100L);

            assertEquals(Pagamento.StatoPagamento.RIMBORSATO, pagamentoValido.getStatoPagamento());
            assertEquals(Prenotazione.StatoPrenotazione.ANNULLATA, prenotazioneValida.getStato());
        }
    }

    @Test
    @DisplayName("Rimborsa Prenotazione: Lancia eccezione per errori anomali e generici di Stripe")
    void testRimborsaPrenotazioneErroreStripe() {
        when(pagamentoRepository.findByPrenotazioneId(100L)).thenReturn(Optional.of(pagamentoValido));

        try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
            StripeException fintaEccezione = mock(StripeException.class);
            when(fintaEccezione.getCode()).thenReturn("carta_scaduta_o_bloccata");

            mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class))).thenThrow(fintaEccezione);

            assertThrows(StripeException.class, () -> pagamentoService.rimborsaPrenotazione(100L));
        }
    }
}