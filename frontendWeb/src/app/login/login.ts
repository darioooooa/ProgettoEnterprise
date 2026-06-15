import { ChangeDetectorRef, Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import { UtenteService } from '../service/utente.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  imports: [
    FormsModule,
    RouterLink,
    CommonModule
  ],
  styleUrls: ['./login.css']
})
export class Login {

  datiAccesso = {
    username: '',
    password: ''
  };

  messaggioErrore: string = '';
  modaleRecuperoAperta: boolean = false;
  emailRecupero: string = '';
  messaggioConferma: boolean = false;

  isLoading: boolean = false;

  constructor(
    private servizioAuth: AutenticazioneService,
    private utenteService: UtenteService,
    private navigatore: Router,
    private cdr: ChangeDetectorRef
  ) {}

  eseguiAccesso() {
    if (this.isLoading) return;

    console.log('Tentativo di accesso per:', this.datiAccesso.username);
    this.messaggioErrore = '';
    this.isLoading = true;

    this.servizioAuth.effettuaAccesso(this.datiAccesso).subscribe({
      next: () => {
        console.log('Login effettuato con successo');
        this.sincronizzaUtenteEReindirizza();
      },
      error: (errore) => {
        this.isLoading = false;
        this.gestisciErrore(errore);
      }
    });
  }

  private sincronizzaUtenteEReindirizza() {
    this.servizioAuth.ottieniDatiUtenteDalDatabase(this.datiAccesso.username).subscribe({
      next: (datiDb) => {
        const utente = Array.isArray(datiDb) ? datiDb[0] : datiDb;
        if (utente?.id) {
          localStorage.setItem('userId', utente.id.toString());
        }
        this.isLoading = false;
        this.indirizzaUtentePerRuolo();
      },
      error: (err) => {
        console.error("Errore sincronizzazione ID:", err);
        this.isLoading = false;
        this.indirizzaUtentePerRuolo();
      }
    });
  }

  private indirizzaUtentePerRuolo() {
    const ruolo = this.servizioAuth.ottieniRuolo();
    console.log('Ruolo intercettato da Keycloak/DB:', ruolo);
    let destinazione = '/home';

    switch (ruolo) {
      case 'ROLE_ADMIN':
        destinazione = '/admin-dashboard';
        break;
      case 'ROLE_ORGANIZZATORE':
        destinazione = '/organizzatore';
        break;
      case 'ROLE_VIAGGIATORE':
      default:
        destinazione = '/home';
        break;
    }

    console.log(`Navigazione difensiva avviata verso: ${destinazione}`);

    this.navigatore.navigate([destinazione]).then((navigatoConSucesso) => {
      if (navigatoConSucesso) {
        this.isLoading = false;
        this.cdr.detectChanges();
      } else {
        console.error("Navigazione interrotta da un guard o fallita.");
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    }).catch((erroreRouter) => {
      console.error("Errore critico durante il routing:", erroreRouter);
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }

  private gestisciErrore(errore: any) {
    console.error('Errore durante il login:', errore);
    const erroreStringa = JSON.stringify(errore);

    if (erroreStringa.includes('Account is not fully set up')) {
      this.messaggioErrore = 'Il tuo account non è ancora attivo! Controlla la tua casella di posta.';
    } else {
      this.messaggioErrore = 'Credenziali non valide, riprova!';
    }

    this.cdr.detectChanges();
  }

  apriModaleRecupero() {
    if (this.isLoading) return;
    this.modaleRecuperoAperta = true;
    this.emailRecupero = '';
    this.messaggioConferma = false;
    this.cdr.detectChanges();
  }

  chiudiModaleRecupero() {
    if (this.isLoading && this.messaggioConferma) return;
    this.modaleRecuperoAperta = false;
    this.cdr.detectChanges();
  }

  inviaEmailRecupero() {
    if (!this.emailRecupero || this.isLoading) return;

    this.isLoading = true;
    this.cdr.detectChanges();

    this.utenteService.recuperaPassword(this.emailRecupero).subscribe({
      next: (risposta) => {
        this.messaggioConferma = true;
        this.isLoading = false;
        this.cdr.detectChanges();
        setTimeout(() => {
          this.chiudiModaleRecupero();
        }, 3000);
      },
      error: (errore) => {
        console.error("Errore durante il recupero password", errore);
        alert("Si è verificato un errore. Assicurati che l'email sia corretta e registrata.");
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
