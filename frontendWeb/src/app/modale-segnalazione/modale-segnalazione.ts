import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SegnalazioneService } from '../service/segnalazione.service';
import { AutenticazioneService } from '../service/autenticazione.service';

@Component({
  selector: 'app-modale-segnalazione',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modale-segnalazione.html',
  styleUrls: ['./modale-segnalazione.css']
})
export class ModaleSegnalazione {

  // Informazioni in entrata
  @Input() visibile: boolean = false;
  @Input() tipoEntita: string = '';
  @Input() idRiferimento: number = 0;

  // Segnale di chiusura in uscita
  @Output() chiudiModale = new EventEmitter<void>();

  motivoSelezionato: string = '';
  descrizione: string = '';
  invioInCorso: boolean = false;

  constructor(
    private segnalazioneService: SegnalazioneService,
    private authService: AutenticazioneService
  ) {}


  invia() {
    if (!this.motivoSelezionato) return;

    const mioIdTesto = this.authService.ottieniId();
    const mioIdNumero = mioIdTesto ? Number(mioIdTesto) : 0;

    if (mioIdNumero === this.idRiferimento) {
      alert("Non puoi segnalare te stesso!");
      return;
    }
    this.invioInCorso = true;

    const datiDaSpedire = {
      tipo: this.tipoEntita,
      idRiferimento: this.idRiferimento,
      motivo: this.motivoSelezionato,
      descrizione: this.descrizione
    };

    this.segnalazioneService.creaSegnalazione(datiDaSpedire, mioIdNumero).subscribe({
      next: () => {
        alert("Segnalazione inviata con successo!");
        this.invioInCorso = false;
        this.annulla();
      },
      error: (errore) => {
        alert("Si è verificato un errore durante l'invio della segnalazione.");
        console.error(errore);
        this.invioInCorso = false;
      }
    });
  }
  annulla() {
    this.motivoSelezionato = '';
    this.descrizione = '';
    this.chiudiModale.emit();
  }
}
