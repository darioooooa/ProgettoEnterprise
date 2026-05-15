import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Viaggio } from '../models/viaggio.model';
import {ViaggioService} from '../service/viaggio-service';
import {HttpClient} from '@angular/common/http';



import {MAPBOX_ACCESS_TOKEN} from '../map/map'; //serve per long e lat


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
    prezzo: 0.0,
    dataInizio: '',
    dataFine: '',
    tappe: [],
    longitudine: 0.0,
    latitudine: 0.0,

  };

  constructor(
    private viaggioService: ViaggioService,
    private router: Router,
    private http: HttpClient,

  ) {}
  //metodo per riempire i campi di latitudine e longitudine semplicemente digitando il nome della città
  //nell'apposito spazio,per far ciò ci appoggiamo all'API di Mapbox,GEOCODING
  cercaCoordinate(){
    const citta=this.nuovoViaggio.destinazione;

    if(citta && citta.length>2){

      const mapurl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(citta)}.json?access_token=${MAPBOX_ACCESS_TOKEN}&limit=1`;
      this.http.get<any>(mapurl).subscribe({
        next: (res) => {
          if (res.features && res.features.length > 0) {
            const [lon, lat] = res.features[0].center;
            this.nuovoViaggio.longitudine = lon;
            this.nuovoViaggio.latitudine = lat;
            console.log('Coordinate ottenute da Mapbox:', lat, lon);
          }
        },
        error: (err) => console.error('Errore Geocoding:', err)
      });
    }

  }
  // Metodo per inviare i dati al controller Spring Boot
  onSubmit() {
    if (this.nuovoViaggio.latitudine === 0 && this.nuovoViaggio.longitudine === 0) {
      alert("Attendi il caricamento della posizione o inserisci una destinazione valida.");
      return;
    }

    this.viaggioService.creaViaggio(this.nuovoViaggio).subscribe({
      next: (res) => {
        console.log('Successo!', res);
        this.router.navigate(['/organizzatore']);
      },
      error: (err) => {
        console.error('Errore durante il salvataggio:', err);
        alert('Controlla i dati inseriti o i permessi (Role ORGANIZZATORE)');
      }
    });
  }
}
