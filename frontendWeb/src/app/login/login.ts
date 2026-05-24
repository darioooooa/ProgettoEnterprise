import {ChangeDetectorRef, Component} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

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

  constructor(
    private servizioAuth: AutenticazioneService,
    private navigatore: Router,
    private cdr: ChangeDetectorRef
  ) {}


  eseguiAccesso() {
    console.log('Tentativo di accesso per:', this.datiAccesso.username);

    // Reset dell'errore precedente
    this.messaggioErrore = '';

    this.servizioAuth.effettuaAccesso(this.datiAccesso).subscribe({
      next: (risposta) => {
        console.log('Login effettuato con successo', risposta);

        const ruolo = this.servizioAuth.ottieniRuolo();

        if (ruolo === 'ROLE_ADMIN') {
          this.navigatore.navigate(['/admin-dashboard']);
        } else {
          this.navigatore.navigate(['/home']);
        }
      },
      error: (errore) => {
        console.error('Errore durante il login:', errore);

        const erroreCorpo = errore.error;
        const erroreStringa = JSON.stringify(errore);

        if (
          (erroreCorpo?.error === 'invalid_grant' && erroreCorpo?.error_description === 'Account is not fully set up') ||
          erroreStringa.includes('Account is not fully set up')
        ) {
          this.messaggioErrore = 'Il tuo account non è ancora attivo! Controlla la tua casella di posta e clicca sul link di conferma per attivarlo.';
        } else {
          this.messaggioErrore = 'Credenziali non valide, riprova!';
        }

        // Forza angular ad aggiornare la schermata visivamente
        this.cdr.detectChanges();
      }
    });
  }
}
