import { ChangeDetectorRef, Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import { UtenteService } from '../service/utente.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
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

  constructor(
    private servizioAuth: AutenticazioneService,
    private utenteService: UtenteService, // <-- INIETTATO QUI
    private navigatore: Router,
    private cdr: ChangeDetectorRef
  ) {}

  eseguiAccesso() {
    console.log('Tentativo di accesso per:', this.datiAccesso.username);
    this.messaggioErrore = '';

    this.servizioAuth.effettuaAccesso(this.datiAccesso).subscribe({
      next: () => {
        console.log('Login effettuato con successo');
        this.sincronizzaUtenteEReindirizza();
      },
      error: (errore) => {
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
        this.indirizzaUtentePerRuolo();
      },
      error: (err) => {
        console.error("Errore sincronizzazione ID:", err);
        this.indirizzaUtentePerRuolo();
      }
    });
  }

  private indirizzaUtentePerRuolo() {
    const ruolo = this.servizioAuth.ottieniRuolo();

    switch (ruolo) {
      case 'ROLE_ADMIN':
        this.navigatore.navigate(['/admin-dashboard']);
        break;
      case 'ROLE_ORGANIZZATORE':
        this.navigatore.navigate(['/organizzatore']);
        break;
      case 'ROLE_VIAGGIATORE':
      default:
        this.navigatore.navigate(['/home']);
        break;
    }
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
    this.modaleRecuperoAperta = true;
    this.emailRecupero = '';
    this.messaggioConferma = false;
    this.cdr.detectChanges();
  }

  chiudiModaleRecupero() {
    this.modaleRecuperoAperta = false;
    this.cdr.detectChanges();
  }

  inviaEmailRecupero() {
    if (!this.emailRecupero) return;

    this.utenteService.recuperaPassword(this.emailRecupero).subscribe({
      next: (risposta) => {
        this.messaggioConferma = true;
        this.cdr.detectChanges();
        setTimeout(() => {
          this.chiudiModaleRecupero();
        }, 3000);
      },
      error: (errore) => {
        console.error("Errore durante il recupero password", errore);
        alert("Si è verificato un errore. Assicurati che l'email sia corretta e registrata.");
        this.cdr.detectChanges();
      }
    });
  }
}
