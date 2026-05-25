import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Prenotazione } from '../models/prenotazioni.model';

@Injectable({
  providedIn: 'root'
})
export class PrenotazioneService {

  // URL del controller
  private readonly API_URL = 'http://localhost:8080/api/v1/prenotazioni';

  constructor(private http: HttpClient) { }

  getListaPrenotazioni(): Observable<Prenotazione[]> {
    return this.http.get<any[]>(this.API_URL);
  }
}
