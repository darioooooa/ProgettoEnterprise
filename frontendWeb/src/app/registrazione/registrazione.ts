import { Component } from '@angular/core';
import { Router,RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-registrazione',
  templateUrl: './registrazione.html',
  imports: [
    FormsModule,CommonModule,RouterLink
  ],
  styleUrls: ['./registrazione.css']
})
export class Registrazione {

  nuovoUtente = {
    username : '',
    nome: '',
    cognome: '',
    email: '',
    password: '',
    ruolo: 'VIAGGIATORE'
  };

  messaggioDiAvviso: string = '';

  constructor(
    private servizioAuth: AutenticazioneService,
    private navigatore: Router
  ) {}

  confermaIscrizione() {
    console.log('OGGETTO CHE STO PER SPEDIRE:', JSON.stringify(this.nuovoUtente));
    this.servizioAuth.registraNuovoUtente(this.nuovoUtente).subscribe({
      next: (risultato) => {
        console.log('Iscrizione completata!', risultato);
        this.navigatore.navigate(['/login']);
      },
      error: (errore) => {
        console.error('Qualcosa è andato storto:', errore);
        this.messaggioDiAvviso = 'Ops! Sembra che questa email sia già registrata.';
      }
    });
  }
}
