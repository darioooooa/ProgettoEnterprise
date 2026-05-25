import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Viaggio } from '../models/viaggio.model';

@Injectable({
  providedIn: 'root'
})
export class ViaggioService {
  private readonly API_URL = 'http://localhost:8080/api/v1/viaggi';

  constructor(private http: HttpClient) { }

  creaViaggio(viaggio: Viaggio): Observable<Viaggio> {

    return this.http.post<Viaggio>(this.API_URL, viaggio);
  }
  getViaggi(page: number = 0, filter?: any): Observable<any> {
    let params = new HttpParams().set('page', page.toString());

    if (filter) {
      Object.keys(filter).forEach(key => {
        if (filter[key] !== null && filter[key] !== undefined) {
          params = params.append(key, filter[key]);
        }
      })
    }

    return this.http.get<any>(this.API_URL, { params } );
  }
  getViaggiPerMappa(): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/mappa-viaggi`);
  }

  getStatisticheRecensioni(viaggioId: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${viaggioId}/statistiche`);
  }

  getGalleria(viaggioId: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${viaggioId}/immagini`);
  }

  getRecensioni(viaggioId: number, page: number = 0, filter?: any): Observable<any> {
    let params = new HttpParams().set('page', page.toString());

    // Filtri dinamici
    if (filter) {
      Object.keys(filter).forEach(key => {
        if (filter[key] !== null && filter[key] !== undefined && filter[key] !== '') {
          params = params.append(key, filter[key]);
        }
      });
    }

    return this.http.get<any>(`${this.API_URL}/${viaggioId}/recensioni`, { params });
  }

  inviaRecensione(viaggioId: number, recensione: { voto: number, commento: string }): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/${viaggioId}/recensioni`, recensione);
  }

  aggiornaRecensione(viaggioId: number, recensioneId: number, recensione: { voto: number, commento: string }): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${viaggioId}/recensioni/${recensioneId}`, recensione);
  }

  eliminaRecensione(viaggioId: number, recensioneId: number): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/${viaggioId}/recensioni/${recensioneId}`);
  }

  aggiungiImmagine(viaggioId: number, url: string, pubblica: boolean = true): Observable<any> {
    const params = new HttpParams().set('url', url).set('pubblica', pubblica.toString());
    return this.http.post<any>(`${this.API_URL}/${viaggioId}/immagini`, {}, { params });
  }

  modificaVisibilita(viaggioId: number, immagineId: number, pubblica: boolean): Observable<any> {
    const params = new HttpParams().set('pubblica', pubblica.toString());

    return this.http.patch<any>(
      `${this.API_URL}/${viaggioId}/immagini/${immagineId}/visibilita`,
      {},
      { params }
    );
  }

  eliminaImmagine(viaggioId: number, immagineId: number): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/${viaggioId}/immagini/${immagineId}`);
  }

  getAttivitaViaggio(viaggioId: number, page: number = 0, filter?: any): Observable<any> {
    let params = new HttpParams().set('page', page.toString());

    if (filter) {
      Object.keys(filter).forEach(key => {
        if (filter[key] !== null && filter[key] !== undefined && filter[key] !== '') {
          params = params.append(key, filter[key]);
        }
      });
    }
    return this.http.get<any>(`${this.API_URL}/${viaggioId}/attivita-viaggi`, { params });
  }

  creaAttivita(viaggioId: number, attivita: any): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/${viaggioId}/attivita-viaggi`, attivita);
  }

  modificaAttivita(viaggioId: number, attivitaId: number, attivita: any): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${viaggioId}/attivita-viaggi/${attivitaId}/modifica`, attivita);
  }

  eliminaAttivita(viaggioId: number, attivitaId: number): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/${viaggioId}/attivita-viaggi/${attivitaId}`);
  }
}
