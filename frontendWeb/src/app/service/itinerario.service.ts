import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Observable, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ItinerarioService {

  private readonly API_URL = '/api/v1/itinerari-preferiti';

  public aggiornaNotifiche = new Subject<void>();

  constructor(private http: HttpClient) { }

  creaLista(richiesta: any): Observable<any> {
    return this.http.post<any>(this.API_URL, richiesta);
  }

  getMieListe(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/mie-liste`);
  }

  getListeCondivise(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/condivise-con-me`);
  }

  cercaListePubbliche(nome: string): Observable<any[]> {
    let params = new HttpParams().set('nome', nome);
    return this.http.get<any[]>(`${this.API_URL}/ricerca-pubblica`, { params });
  }

  getDettaglioLista(id: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}`);
  }

  cambiaVisibilita(id: number, nuovaVisibilita: string): Observable<any> {
    let params = new HttpParams().set('nuovaVisibilita', nuovaVisibilita);
    return this.http.patch<any>(`${this.API_URL}/${id}/visibilita`, {}, { params });
  }

  eliminaLista(id: number): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/${id}`);
  }

  aggiungiViaggioAItinerario(idLista: number, idViaggio: number): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/${idLista}/viaggi/${idViaggio}`, {});
  }

  rimuoviViaggio(idLista: number, idViaggio: number): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/${idLista}/viaggi/${idViaggio}`);
  }

  spostaViaggio(idSorgente: number, idDestinazione: number, idViaggio: number) {
    return this.http.post(`/api/v1/itinerari-preferiti/${idSorgente}/sposta-in/${idDestinazione}/viaggi/${idViaggio}`, {});
  }

  getListePubblicheUtente(username: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/utente/${username}/pubblici`);
  }
  //PER ITINERARI CONDIVISI
  invitaCollaboratore(idItinerario: number, emailAmico: string): Observable<any> {
    const payload = { email: emailAmico };
    return this.http.post(`${this.API_URL}/${idItinerario}/invita`, payload);
  }

  accettaInvito(idItinerario: number): Observable<any> {
    return this.http.post(`${this.API_URL}/${idItinerario}/accetta-invito`, {});
  }
  getInvitiInSospeso(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/inviti`);
  }
  getItinerariCondivisiConMe(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/condivisi`);
  }
  rifiutaInvito(idItinerario: number): Observable<any> {
    return this.http.post(`${this.API_URL}/${idItinerario}/rifiuta-invito`, {});
  }
}
