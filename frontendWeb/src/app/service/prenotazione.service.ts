import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Prenotazione } from '../models/prenotazioni.model';
import { AutenticazioneService } from './autenticazione.service';

@Injectable({
  providedIn: 'root'
})
export class PrenotazioneService {

  // URL del controller
  private readonly API_URL = '/api/v1/prenotazioni';

  constructor(private http: HttpClient, private authService: AutenticazioneService) {
  }

  getListaPrenotazioni(): Observable<Prenotazione[]> {
    return this.http.get<any[]>(this.API_URL);
  }

  scaricaFileIcs(idPrenotazione: number): Observable<Blob> {
    const token = this.authService.ottieniToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    const urlIcs = `${this.API_URL}/${idPrenotazione}/esporta-calendario`;

    return this.http.get(urlIcs, {
      headers: headers,
      responseType: 'blob'
    });
  }

  creaPrenotazione(viaggioId: number, numeroPersone: number): Observable<any> {
    const url = `${this.API_URL}/viaggi/${viaggioId}/prenota`;
    return this.http.post(url, { numeroPersone: numeroPersone });
  }

  cancellaPrenotazione(id: number): Observable<any> {
    return this.http.delete(`${this.API_URL}/${id}`);
  }

}
