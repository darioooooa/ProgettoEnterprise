import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {loadStripe, Stripe, StripeCardElement, StripeElements} from '@stripe/stripe-js';
import {PagamentoService} from '../service/pagamento.service';
import {ActivatedRoute, Route, Router} from '@angular/router';
import {environment} from '../../environments/development';
import {response} from 'express';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-sezione-pagamento',
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './sezione-pagamento.html',
  styleUrl: './sezione-pagamento.css',
})
export class SezionePagamentoComponent implements OnInit{
  titolareCarta: string='';
  isProcessing:boolean=false;
  errorePagamento: string| null=null

  stripe: Stripe | null = null;
  elements: StripeElements | null = null;
  card: StripeCardElement | null = null;
  clientSecret: string = '';

  idPrenotazioneAttuale!: number;

  constructor(private pagamentoService: PagamentoService,
              private route: ActivatedRoute,
              private router:Router,
              private cdr: ChangeDetectorRef
  ){}
  //uso async perche con stripe serve per utilizzare le Promises ed evitare un sacco di controlli
  //per connetterci a stripe
  async ngOnInit() {
    const idUrl = this.route.snapshot.paramMap.get('id');

    if (!idUrl) {
      this.errorePagamento = "ID prenotazione mancante. Ritorna alla pagina precedente.";
      return;
    }
    this.idPrenotazioneAttuale = Number(idUrl)

    //inizializzo Stripe
    this.stripe = await loadStripe(environment.stripePublicKey);
    this.pagamentoService.creaPaymentIntent(this.idPrenotazioneAttuale).subscribe({
      next: (response) => {
        this.clientSecret = response.clientSecret;
        this.montaStripeCard();
      },
      error: (err) => {
        setTimeout(() => {
          this.errorePagamento = 'Errore di connessione al server di pagamento.';
          this.cdr.detectChanges();
        });
      }
    });
  }
  montaStripeCard() {
    if (!this.stripe) return;
    this.elements = this.stripe.elements();

    const stileVetro = {
      base: {
        color: '#ffffff',
        fontFamily: '"Poppins", sans-serif',
        fontSmoothing: 'antialiased',
        fontSize: '16px',
        '::placeholder': { color: 'rgba(255, 255, 255, 0.6)' }
      },
      invalid: {
        color: '#ff8a80',
        iconColor: '#ff8a80'
      }
    };

    this.card = this.elements.create('card', { style: stileVetro });
    this.card.mount('#card-element');

    this.card.on('change', (event) => {
      this.errorePagamento = event.error ? event.error.message : null;
    });
  }
  async pagaOra() {
    if (!this.stripe || !this.card || !this.clientSecret) return;

    this.isProcessing = true;
    this.errorePagamento = null;

    const risultato = await this.stripe.confirmCardPayment(this.clientSecret, {
      payment_method: {
        card: this.card,
        billing_details: { name: this.titolareCarta }
      }
    });

    if (risultato.error) {
      this.errorePagamento = risultato.error.message || 'Pagamento rifiutato.';
      this.isProcessing = false;
    } else if (risultato.paymentIntent && risultato.paymentIntent.status === 'succeeded') {

      // Creo il DTO coi nomi che hai scelto tu
      const ricevuta = {
        idPrenotazione: this.idPrenotazioneAttuale,
        importo: risultato.paymentIntent.amount / 100,
        ricevutaPagamento: risultato.paymentIntent.id,
        titolareCarta: this.titolareCarta
      };

      this.pagamentoService.confermaPagamento(ricevuta).subscribe({
        next: () => {
          this.isProcessing = false;
          alert('🎉 Pagamento confermato! La tua prenotazione è valida.');
          this.router.navigate(['/prenotazioni']);
        },
        error: (err) => {
          this.isProcessing = false;
          this.errorePagamento = "Pagamento completato, ma errore nel salvare la ricevuta sul server.";
          console.error(err);
        }
      });
    }
  }

}
