import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Viaggio } from '../models/viaggio.model';
import {ViaggioService} from '../service/viaggio.service';
import {HttpClient} from '@angular/common/http';
import {MAPBOX_ACCESS_TOKEN} from '../map/map'; //serve per long e lat


@Component({
  selector: 'app-crea-viaggio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crea-viaggio.html',
  styleUrl: './crea-viaggio.css',
})
export class CreaViaggio {

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
    cittaPartenza: ''
  };

  isLoading: boolean = false;

  constructor(
    private viaggioService: ViaggioService,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

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

  aggiungiTappa() {
    if (this.isLoading) return;
    this.nuovoViaggio.tappe.push({
      titolo: '',
      descrizione: '',
      orarioInizio: '',
      orarioFine: '',
      posizione: '',
      costo: 0
    });
  }

  rimuoviTappa(index: number) {
    if (this.isLoading) return;
    this.nuovoViaggio.tappe.splice(index, 1);
  }

  trackByTappa(index: number, item: any) {
    return index;
  }

  // Intercetta il submit in modo interattivo e asincrono
  async onSubmit() {
    if (this.nuovoViaggio.latitudine === 0 && this.nuovoViaggio.longitudine === 0) {
      const coordinateTrovate = await this.cercaCoordinate();
      if (!coordinateTrovate) {
        alert("Inserisci una destinazione valida prima di pubblicare.");
        return;
      }
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    this.viaggioService.creaViaggio(this.nuovoViaggio).subscribe({
      next: (res) => {
        console.log('Successo!', res);
        this.isLoading = false;
        this.router.navigate(['/organizzatore']);
      },
      error: (err) => {
        console.error('Errore durante il salvataggio:', err);
        alert('Controlla i dati inseriti o i permessi (Role ORGANIZZATORE)');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
