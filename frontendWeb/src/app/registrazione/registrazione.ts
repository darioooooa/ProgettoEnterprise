import { Component, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registrazione',
  standalone: true,
  templateUrl: './registrazione.html',
  imports: [
    FormsModule, CommonModule, RouterLink
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
  isLoading: boolean = false;
  registrazioneCompletata: boolean = false;
  emailInserita: string = '';

  constructor(
    private servizioAuth: AutenticazioneService,
    private navigatore: Router,
    private cdr: ChangeDetectorRef
  ) {}

  confermaIscrizione() {
    if (this.isLoading) return;

    console.log('OGGETTO CHE STO PER SPEDIRE:', JSON.stringify(this.nuovoUtente));

    this.messaggioDiAvviso = '';
    this.isLoading = true;
    this.cdr.detectChanges();

    this.servizioAuth.registraNuovoUtente(this.nuovoUtente).subscribe({
      next: (risultato) => {
        console.log('Iscrizione completata!', risultato);

        this.emailInserita = this.nuovoUtente.email;
        this.registrazioneCompletata = true;
        this.isLoading = false;

        this.cdr.detectChanges();
      },
      error: (errore) => {
        console.error('Qualcosa è andato storto:', errore);
        this.isLoading = false;
        this.messaggioDiAvviso = 'Ops! Sembra che questa email o username siano già registrati.';

        this.cdr.detectChanges();
      }
    });
  }
}
