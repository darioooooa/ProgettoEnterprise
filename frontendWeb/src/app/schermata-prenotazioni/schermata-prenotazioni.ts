import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-schermata-prenotazioni',
  standalone: true,
  imports: [CommonModule ],
  templateUrl: './schermata-prenotazioni.html',
  styleUrl: './schermata-prenotazioni.css'
})
export class SchermataPrenotazioni {

  // Dati statici poi cambiamo con dati del db
  listaPrenotazioni = [
    {
      destinazione: 'Bali, Indonesia',
      dataPartenza: '2025-05-12',
      prezzoTotale: 1250.00,
      stato: 'Confermata',
      codicePrenotazione: 'TRV-8821'
    },
    {
      destinazione: 'Sahara, Marocco',
      dataPartenza: '2025-08-05',
      prezzoTotale: 890.50,
      stato: 'In Attesa',
      codicePrenotazione: 'TRV-4432'
    },
    {
      destinazione: 'Dolomiti, Italia',
      dataPartenza: '2025-12-20',
      prezzoTotale: 450.00,
      stato: 'Confermata',
      codicePrenotazione: 'TRV-1109'
    }
  ];

  constructor() {

  }
}
