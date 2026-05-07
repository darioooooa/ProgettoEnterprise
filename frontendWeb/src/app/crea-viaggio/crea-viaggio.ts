import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Viaggio } from '../models/viaggio.model';
import {ViaggioService} from '../service/viaggio-service';

@Component({
  selector: 'app-crea-viaggio',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './crea-viaggio.html',
  styleUrl: './crea-viaggio.css',
})
export class CreaViaggio {


  nuovoViaggio: Viaggio = {
    titolo: '',
    descrizione: '',
    destinazione: '',
    prezzo: 0,
    dataInizio: '',
    dataFine: '',
    tappe: []
  };

  constructor(
    private viaggioService: ViaggioService,
    private router: Router
  ) {}
  // Metodo per inviare i dati al controller Spring Boot
  onSubmit() {
    this.viaggioService.creaViaggio(this.nuovoViaggio).subscribe({
      next: (res) => {
        console.log('Successo!', res);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Errore durante il salvataggio:', err);
        alert('Controlla i dati inseriti o i permessi (Role ORGANIZZATORE)');
      }
    });
  }
}
