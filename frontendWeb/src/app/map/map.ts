import {AfterViewInit, Component,PLATFORM_ID,inject} from '@angular/core';
import {ViaggioService} from '../service/viaggio-service';
import mapboxgl from 'mapbox-gl';
import {isPlatformBrowser} from '@angular/common';
import{envMap} from '../../environments/envMap';

export const MAPBOX_ACCESS_TOKEN = envMap.mapboxToken;

@Component({
  selector: 'app-map',
  imports: [],
  templateUrl: './map.html',
  styleUrl: './map.css',
})
export class MapComponent implements AfterViewInit{
    map:mapboxgl.Map | undefined;
    style='mapbox://styles/mapbox/streets-v11';
    lat = 41.8902; // Centro iniziale
    lng = 12.4922

    private platformId=inject(PLATFORM_ID);
    constructor(private viaggioService: ViaggioService) {
      //è il token per accedere a mapbox
      (mapboxgl as any).accessToken = MAPBOX_ACCESS_TOKEN;
    }
  ngAfterViewInit(): void {

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

    // Carichiamo i dati solo dopo che la mappa è pronta
    this.map.on('load', () => {
      this.caricaViaggiPerMappa();
  });
    }
  caricaViaggiPerMappa() {
    this.viaggioService.getViaggiPerMappa().subscribe({
      next: (viaggi) => {
        viaggi.forEach((v: any) => {
          const popup = new mapboxgl.Popup({ offset: 25 }).setHTML(
            `<h3>${v.titolo}</h3><button>Vedi dettagli</button>`
          );

          new mapboxgl.Marker({ color: 'red' })
            .setLngLat([v.longitudine, v.latitudine])
            .setPopup(popup)
            .addTo(this.map!);
        });
      },
      error: (err) => {
        console.error("Errore nel recupero viaggi per mappa:", err);
      }
    });
  }

}
