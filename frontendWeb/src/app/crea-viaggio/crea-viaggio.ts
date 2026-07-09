import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Viaggio } from '../models/viaggio.model';
import { ViaggioService } from '../service/viaggio.service';
import { HttpClient } from '@angular/common/http';
import { MAPBOX_ACCESS_TOKEN } from '../map/map'; //serve per long e lat

@Component({
  selector: 'app-crea-viaggio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crea-viaggio.html',
  styleUrl: './crea-viaggio.css',
})
export class CreaViaggio implements OnInit {

  nuovoViaggio: Viaggio = {
    titolo: '',
    descrizione: '',
    destinazione: '',
    prezzo: 0.0,
    dataInizio: '',
    dataFine: '',
    tappe: [],
    longitudine: 0.0,
    latitudine: 0.0,
    maxPartecipanti: 10,
    partecipantiAttuali: 0,
    cittaPartenza: '',
    stato: 'APERTO'
  };

  isLoading: boolean = false;

  tagDisponibili: string[] = [];
  tagSelezionati: string[] = [];
  isLoadingTag: boolean = false;
  maxTagSelezionabili = 3;
  minTagRichiesti = 1;

  private readonly emojiMap: { [key: string]: string } = {
    'Mare': '️🌊',
    'Montagna': '️⛰️',
    'Città d\'arte': '🎨',
    'Relax': '🧘',
    'Avventura': '🏕️',
    'Cultura': '🏛️',
    'Enogastronomia': '🍷',
    'Economico': '💰',
    'Lusso': '💎',
    'Inverno': '️❄️',
    'Estate': '☀️'
  };

  constructor(
    private viaggioService: ViaggioService,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.caricaTagDisponibili();
  }

  caricaTagDisponibili() {
    this.isLoadingTag = true;
    this.viaggioService.getTagDisponibili().subscribe({
      next: (tags) => {
        this.tagDisponibili = tags;
        this.isLoadingTag = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei tag', err);
        this.isLoadingTag = false;
        this.tagDisponibili = [
          'Mare', 'Montagna', 'Città d\'arte', 'Relax',
          'Avventura', 'Cultura', 'Enogastronomia',
          'Economico', 'Lusso', 'Inverno', 'Estate'
        ];
        this.cdr.detectChanges();
      }
    });
  }

  toggleTag(tag: string) {
    const index = this.tagSelezionati.indexOf(tag);

    if (index > -1) {
      this.tagSelezionati.splice(index, 1);
    } else {
      if (this.tagSelezionati.length < this.maxTagSelezionabili) {
        this.tagSelezionati.push(tag);
      } else {
        alert(`Puoi selezionare al massimo ${this.maxTagSelezionabili} tag`);
      }
    }
    this.cdr.detectChanges();
  }

  isTagSelezionato(tag: string): boolean {
    return this.tagSelezionati.includes(tag);
  }

  puoSelezionareTag(): boolean {
    return this.tagSelezionati.length < this.maxTagSelezionabili;
  }

  getTagIcon(tag: string): string {
    return this.emojiMap[tag] || '️';
  }

  cercaCoordinate(): Promise<boolean> {
    return new Promise((resolve) => {
      const citta = this.nuovoViaggio.destinazione?.trim();
      if (!citta || citta.length <= 2) {
        resolve(false);
        return;
      }
      this.isLoading = true;
      this.cdr.detectChanges();
      const mapurl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(citta)}.json?access_token=${MAPBOX_ACCESS_TOKEN}&limit=1`;
      this.http.get<any>(mapurl).subscribe({
        next: (res) => {
          if (res.features && res.features.length > 0) {
            const [lon, lat] = res.features[0].center;
            this.nuovoViaggio.longitudine = lon;
            this.nuovoViaggio.latitudine = lat;
            console.log('Coordinate ottenute da Mapbox:', lat, lon);
            this.isLoading = false;
            this.cdr.detectChanges();
            resolve(true);
          } else {
            this.nuovoViaggio.longitudine = 0.0;
            this.nuovoViaggio.latitudine = 0.0;
            this.isLoading = false;
            this.cdr.detectChanges();
            alert("Destinazione non riconosciuta da Mapbox.");
            resolve(false);
          }
        },
        error: (err) => {
          console.error('Errore Geocoding:', err);
          this.isLoading = false;
          this.cdr.detectChanges();
          resolve(false);
        }
      });
    });
  }

  // Verifica la correttezza di una singola tappa inserita
  validaTappa(tappa: any, index: number): string | null {
    if (!tappa.titolo || !tappa.titolo.trim()) {
      return `Tappa ${index + 1}: Il titolo della tappa è obbligatorio.\n`;
    }
    if (!tappa.orarioInizio || !tappa.orarioFine) {
      return `Tappa ${index + 1}: Selezionare sia l'orario di inizio che l'orario di fine.\n`;
    }
    const inizio = new Date(tappa.orarioInizio);
    const fine = new Date(tappa.orarioFine);
    if (fine <= inizio) {
      return `Tappa ${index + 1}: L'orario di fine deve essere successivo all'orario di inizio.`;
    }
    if (!tappa.posizione || !tappa.posizione.trim()) {
      return `Tappa ${index + 1}: La posizione è obbligatoria.`;
    }
    if (tappa.costo === null || tappa.costo === undefined || tappa.costo < 0) {
      return `Tappa ${index + 1}: Il costo deve essere un valore maggiore o uguale a 0.`;
    }
    // Controllo di coerenza con le date del viaggio padre
    if (this.nuovoViaggio.dataInizio && this.nuovoViaggio.dataFine) {
      const inizioViaggio = new Date(this.nuovoViaggio.dataInizio + 'T00:00:00');
      const fineViaggio = new Date(this.nuovoViaggio.dataFine + 'T23:59:59');
      if (inizio < inizioViaggio) {
        return `Tappa ${index + 1}: L'attività non può iniziare prima della partenza del viaggio.`;
      }
      if (fine > fineViaggio) {
        return `Tappa ${index + 1}: L'attività non può terminare dopo il ritorno del viaggio.`;
      }
    }
    return null;
  }

  aggiungiTappa() {
    if (this.isLoading) return;
    // Prima di aggiungere una nuova tappa, si valida la precedente
    if (this.nuovoViaggio.tappe.length > 0) {
      const ultimoIndex = this.nuovoViaggio.tappe.length - 1;
      const errore = this.validaTappa(this.nuovoViaggio.tappe[ultimoIndex], ultimoIndex);
      if (errore) {
        alert(errore);
        return;
      }
    }
    this.nuovoViaggio.tappe.push({
      titolo: '',
      descrizione: '',
      orarioInizio: '',
      orarioFine: '',
      posizione: '',
      costo: 0
    });
    this.cdr.detectChanges();
  }

  rimuoviTappa(index: number) {
    if (this.isLoading) return;
    this.nuovoViaggio.tappe.splice(index, 1);
    this.cdr.detectChanges();
  }

  trackByTappa(index: number, item: any) {
    return index;
  }

  async onSubmit() {
    if (this.tagSelezionati.length < this.minTagRichiesti) {
      alert(`Seleziona almeno ${this.minTagRichiesti} tag per il viaggio`);
      return;
    }

    // Controlli anagrafica base del viaggio
    if (!this.nuovoViaggio.titolo.trim() || !this.nuovoViaggio.dataInizio || !this.nuovoViaggio.dataFine) {
      alert("Compila tutti i campi obbligatori del viaggio.");
      return;
    }
    const inizioV = new Date(this.nuovoViaggio.dataInizio);
    const fineV = new Date(this.nuovoViaggio.dataFine);
    if (fineV < inizioV) {
      alert("La data di fine viaggio non può precedere la data di inizio.");
      return;
    }
    for (let i = 0; i < this.nuovoViaggio.tappe.length; i++) {
      const erroreTappa = this.validaTappa(this.nuovoViaggio.tappe[i], i);
      if (erroreTappa) {
        alert(erroreTappa);
        return;
      }
    }
    if (this.nuovoViaggio.latitudine === 0 && this.nuovoViaggio.longitudine === 0) {
      const coordinateTrovate = await this.cercaCoordinate();
      if (!coordinateTrovate) {
        alert("Inserisci una destinazione valida prima di pubblicare.");
        return;
      }
    }

    (this.nuovoViaggio as any).tags = [...this.tagSelezionati];

    this.isLoading = true;
    this.cdr.detectChanges();
    this.viaggioService.creaViaggio(this.nuovoViaggio).subscribe({
      next: (res) => {
        console.log('Viaggio e tappe salvati con successo!', res);
        this.isLoading = false;
        this.router.navigate(['/organizzatore']);
      },
      error: (err) => {
        console.error('Errore durante il salvataggio:', err);
        alert(err.error?.message || 'Controlla i dati inseriti. Verifica la coerenza delle date del programma.');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
