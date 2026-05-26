import { AfterViewInit, Component, PLATFORM_ID, inject, EventEmitter, Output } from '@angular/core';

import mapboxgl from 'mapbox-gl';
import { isPlatformBrowser } from '@angular/common';
import { envMap } from '../../environments/envMap';
import {ViaggioService} from '../service/viaggio.service'; // Controlla che questo percorso sia corretto nel tuo progetto

export const MAPBOX_ACCESS_TOKEN = envMap.mapboxToken;

@Component({
  selector: 'app-map',
  imports: [],
  templateUrl: './map.html',
  styleUrl: './map.css',
})
export class MapComponent implements AfterViewInit {
  map: mapboxgl.Map | undefined;
  style = 'mapbox://styles/mapbox/streets-v11';
  lat = 41.8902; // Centro iniziale
  lng = 12.4922;


  @Output() viaggioSelezionato = new EventEmitter<number>();

  private platformId = inject(PLATFORM_ID);

  constructor(private viaggioService: ViaggioService) {
    // Configurazione iniziale del token Mapbox
    (mapboxgl as any).accessToken = MAPBOX_ACCESS_TOKEN;
  }

  ngAfterViewInit(): void {
    // Protezione SSR per evitare crash quando Angular gira lato server (Node.js)
    if (isPlatformBrowser(this.platformId)) {
      this.inizializzaMappa();
    }
  }

  private inizializzaMappa(): void {
    this.map = new mapboxgl.Map({
      container: 'map-display',
      style: this.style,
      center: [this.lng, this.lat],
      zoom: 5
    });
    this.map.addControl(new mapboxgl.NavigationControl());

    this.map.on('load', () => {
      this.caricaViaggiPerMappa();
    });
  }

  caricaViaggiPerMappa() {
    this.viaggioService.getViaggiPerMappa().subscribe({
      next: (viaggi) => {
        viaggi.forEach((v: any) => {
          this.aggiungiMarkerERelativoPopup(v);
        });
      },
      error: (err) => console.error("Errore nel caricamento dei marker sulla mappa:", err)
    });
  }

  private aggiungiMarkerERelativoPopup(v: any): void {
    //  Creiamo a mano il DIV del popup (Evita accumuli di eventi nel DOM di Mapbox)
    const divPopup = document.createElement('div');
    divPopup.innerHTML = `
      <h3 style="margin-bottom: 8px; color: #0f172a; font-family: sans-serif; font-size: 1.1rem;">${v.titolo}</h3>
      <button style="background:#3b82f6; color:white; border:none; padding:6px 12px; border-radius:6px; cursor:pointer; width:100%; font-weight: 600; font-family: sans-serif;">
        Vedi dettagli
      </button>
    `;

    // Intercettiamo subito il click sul bottone legandolo matematicamente all'ID di questo viaggio (v.id)
    const bottone = divPopup.querySelector('button');
    if (bottone) {
      bottone.addEventListener('click', (e) => {
        e.stopPropagation();
        e.preventDefault();

        console.log("👉 ID Viaggio cliccato con certezza matematica dalla mappa:", v.id);

        // Spara l'ID verso l'alto (al componente padre)
        this.viaggioSelezionato.emit(v.id);
      });
    }


    const popup = new mapboxgl.Popup({ offset: 25 }).setDOMContent(divPopup);

    // Creiamo il Marker rosso e lo leghiamo alla mappa e al suo popup
    new mapboxgl.Marker({ color: 'red' })
      .setLngLat([v.longitudine, v.latitudine])
      .setPopup(popup)
      .addTo(this.map!);
  }
}
