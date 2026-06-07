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


  @Output() viaggioSelezionato = new EventEmitter<any>();

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
      next: (viaggi: any) => {
        if (viaggi && viaggi.length > 0) {
          this.aggiungiMarkerIntelligenti(viaggi);
        }

      },
      error: (err) => console.error("Errore nel caricamento dei viaggi sulla mappa:", err)
    });
  }

  private aggiungiMarkerIntelligenti(viaggi: any[]): void {
    // VIAGGI CHE HANNO ESATTAMENTE LE STESSE COORDINATE
    const mappaCoordinate = new Map<string, any[]>();

    viaggi.forEach(v => {
      if (v.latitudine != null && v.longitudine != null) {
        // chiave unica usando lat e lng
        const lat = Number(v.latitudine).toFixed(4);
        const lng = Number(v.longitudine).toFixed(4);
        const chiave = `${lat}_${lng}`;

        if (!mappaCoordinate.has(chiave)) {
          mappaCoordinate.set(chiave, []);
        }
        mappaCoordinate.get(chiave)!.push(v);
      }
    });


    mappaCoordinate.forEach((listaViaggi, chiave) => {
      // Estraiamo le coordinate
      const lat = parseFloat(listaViaggi[0].latitudine);
      const lng = parseFloat(listaViaggi[0].longitudine);

      const divPopup = document.createElement('div');


      // se ci sono più viaggi nella stessa posizione
      if (listaViaggi.length > 1) {
        divPopup.innerHTML = `
          <h3 style="margin-bottom: 8px; color: #0f172a; font-family: sans-serif; font-size: 1.1rem; text-align: center;">
            🗺️ Ci sono ${listaViaggi.length} viaggi in questa posizione
          </h3>
          <button style="background:#1a56db; color:white; border:none; padding:8px 12px; border-radius:6px; cursor:pointer; width:100%; font-weight: 600; font-family: sans-serif;">
            Visualizza i vari viaggi
          </button>
        `;

        const bottone = divPopup.querySelector('button');
        if (bottone) {
          bottone.addEventListener('click', (e) => {
            e.stopPropagation();
            e.preventDefault();
            // manda l'array di viaggi
            this.viaggioSelezionato.emit(listaViaggi);
          });
        }

        const popup = new mapboxgl.Popup({ offset: 25 }).setDOMContent(divPopup);


        new mapboxgl.Marker({ color: '#1a56db' })
          .setLngLat([lng, lat])
          .setPopup(popup)
          .addTo(this.map!);
      }

        // VIAGGIO SINGOLO

      else {
        const viaggioSingolo = listaViaggi[0];

        divPopup.innerHTML = `
          <h3 style="margin-bottom: 8px; color: #0f172a; font-family: sans-serif; font-size: 1.1rem; text-align: center;">
            ${viaggioSingolo.titolo}
          </h3>
          <button style="background:#ef4444; color:white; border:none; padding:8px 12px; border-radius:6px; cursor:pointer; width:100%; font-weight: 600; font-family: sans-serif;">
            Vedi dettagli
          </button>
        `;

        const bottone = divPopup.querySelector('button');
        if (bottone) {
          bottone.addEventListener('click', (e) => {
            e.stopPropagation();
            e.preventDefault();
            // manda SOLO IL SINGOLO VIAGGIO
            this.viaggioSelezionato.emit(viaggioSingolo);
          });
        }

        const popup = new mapboxgl.Popup({ offset: 25 }).setDOMContent(divPopup);


        new mapboxgl.Marker({ color: '#ef4444' })
          .setLngLat([lng, lat])
          .setPopup(popup)
          .addTo(this.map!);
      }
    });
  }
}
