import { Component } from '@angular/core';
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
    private navigatore: Router
  ) {}


  eseguiAccesso() {
    console.log('Tentativo di accesso per:', this.datiAccesso.username);
    this.servizioAuth.effettuaAccesso(this.datiAccesso).subscribe({
      next: (risposta) => {
        console.log('Login effettuato con successo', risposta);

        const ruolo = risposta.ruolo;

        if (ruolo === 'ROLE_ADMIN') {
          this.navigatore.navigate(['/admin-dashboard']);
        } else {
          this.navigatore.navigate(['/home']);
        }
      },
      error: (errore) => {
        console.error('Errore durante il login:', errore);
        this.messaggioErrore = 'Credenziali non valide, riprova!';
      }
    });
  }
}
