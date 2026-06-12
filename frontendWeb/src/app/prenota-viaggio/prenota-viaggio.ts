import {Component, OnInit, ChangeDetectorRef, NgZone} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PrenotazioneService } from '../service/prenotazione.service';
import { ViaggioService } from '../service/viaggio.service';

@Component({
  selector: 'app-prenota-viaggio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './prenota-viaggio.html',
  styleUrls: ['./prenota-viaggio.css']
})
export class PrenotaViaggioComponent implements OnInit {

  viaggioId!: number;
  dettagliViaggio: any = null;

  numeroPersone: number = 1;
  prezzoTotale: number = 0;

  messaggioErrore: string = '';
  messaggioSuccesso: string = '';

  constructor(
    private rottaAttuale: ActivatedRoute,
    private prenotazioneService: PrenotazioneService,
    private viaggioService: ViaggioService,
    private navigatore: Router,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.rottaAttuale.paramMap.subscribe(params => {
      const id = params.get('id');

      if (id) {
        this.viaggioId = Number(id);
        this.caricaDettagliViaggio();
      } else {
        this.messaggioErrore = "ID viaggio mancante nell'URL.";
        this.cdr.detectChanges();
      }
    });
  }

  caricaDettagliViaggio() {
    this.viaggioService.getViaggioById(this.viaggioId).subscribe({
      next: (viaggio: any) => {
        this.dettagliViaggio = viaggio;
        this.calcolaPrezzo();
        console.log("Dati ricevuti dal backend:", viaggio);
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.messaggioErrore = "Impossibile caricare i dettagli del viaggio.";
        this.cdr.detectChanges();
      }
    });
  }

  aumentaPersone() {
    const postiRimasti = this.dettagliViaggio.maxPartecipanti - this.dettagliViaggio.partecipantiAttuali;
    if (this.numeroPersone < postiRimasti) {
      this.numeroPersone++;
      this.calcolaPrezzo();
    }
  }

  diminuisciPersone() {
    if (this.numeroPersone > 1) {
      this.numeroPersone--;
      this.calcolaPrezzo();
    }
  }

  calcolaPrezzo() {
    if (this.dettagliViaggio && this.dettagliViaggio.prezzo) {
      this.prezzoTotale = this.dettagliViaggio.prezzo * this.numeroPersone;
    }
  }

  confermaPrenotazione() {
    this.messaggioErrore = '';
    this.messaggioSuccesso = '';

    this.prenotazioneService.creaPrenotazione(this.viaggioId, this.numeroPersone).subscribe({
      next: (risposta: any) => {
        this.messaggioSuccesso = "Prenotazione inviata! Trovi i dettagli nella tua area personale.";
        this.cdr.detectChanges();

        setTimeout(() => {
          this.navigatore.navigate(['/mie-prenotazioni']);
        }, 3000);
      },
      error: (errore: any) => {
        this.ngZone.run(() => {
          let estratto = "Errore durante la prenotazione. Riprova più tardi.";
          if (errore.status === 400 || errore.status === 409) {
            if (errore.error && errore.error.message) {
              estratto = errore.error.message;
            }
            else if (typeof errore.error === 'string') {
              estratto = errore.error;
            }
            else {
              estratto = "Attenzione: Esiste già un viaggio prenotato per questa data o i posti sono esauriti!";
            }
          }

          this.messaggioErrore = estratto;
          this.cdr.detectChanges();
        });
      }    });
  }
}
