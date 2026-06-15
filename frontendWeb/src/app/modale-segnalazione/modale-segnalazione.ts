import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
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

  isLoading: boolean = false;

  constructor(
    private segnalazioneService: SegnalazioneService,
    private authService: AutenticazioneService,
    private cdr: ChangeDetectorRef
  ) {}

  invia() {
    if (!this.motivoSelezionato || this.isLoading) return;

    const mioIdTesto = this.authService.ottieniId();
    const mioIdNumero = mioIdTesto ? Number(mioIdTesto) : 0;

    if (mioIdNumero === this.idRiferimento && this.tipoEntita === 'UTENTE') {
      alert("Non puoi segnalare te stesso!");
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    const datiDaSpedire = {
      tipo: this.tipoEntita,
      idRiferimento: this.idRiferimento,
      motivo: this.motivoSelezionato,
      descrizione: this.descrizione
    };

    this.segnalazioneService.creaSegnalazione(datiDaSpedire, mioIdNumero).subscribe({
      next: () => {
        alert("Segnalazione inviata con successo!");
        this.isLoading = false;
        this.annulla();
      },
      error: (errore) => {
        alert("Si è verificato un errore durante l'invio della segnalazione.");
        console.error(errore);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  annulla() {
    if (this.isLoading) return;

    this.motivoSelezionato = '';
    this.descrizione = '';
    this.chiudiModale.emit();
  }
}
