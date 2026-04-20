import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-schermata-home', // Il nome del "tag" HTML
  standalone: true,              // Indica che il componente si gestisce da solo
  imports: [CommonModule],       // Permette di usare direttive come *ngFor o *ngIf
  templateUrl: './schermataHome.html',
  styleUrl: './schermataHome.css'
})
export class SchermataHomeComponent {
  // Titolo della pagina per il progetto Enterprise
  titolo = 'Benvenuti in TravelBooking';

  // Esempio di dati "mock" per i viaggi (come suggerito dalla traccia)
  viaggiEsempio = [
    { destinazione: 'Roma', descrizione: 'Tour del Colosseo', prezzo: 45 },
    { destinazione: 'Parigi', descrizione: 'Crociera sulla Senna', prezzo: 60 }
  ];

  constructor() {
    console.log('Schermata Home inizializzata!');
  }
}
