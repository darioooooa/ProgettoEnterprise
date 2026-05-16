import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AmiciziaService {

  private readonly baseUrl = 'http://localhost:8080/api/v1/amicizie';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  private creaHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('token_accesso');
      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      }
    }
    return headers;
  }

  // CORRETTO: Adesso passa la stringa e gli header sono formattati bene
  inviaRichiesta(riceventeUsername: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/richiesta/${riceventeUsername}`, {}, { headers: this.creaHeaders() });
  }

  accettaRichiesta(amiciziaId: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/${amiciziaId}/accetta`, {}, { headers: this.creaHeaders() });
  }

  ottieniMieiAmici(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/miei-amici`, { headers: this.creaHeaders() });
  }

  ottieniRichiesteRicevute(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/richieste/ricevute`, { headers: this.creaHeaders() });
  }

  ottieniRichiesteInviate(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/richieste/inviate`, { headers: this.creaHeaders() });
  }

  rifiutaRichiesta(amiciziaId: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/${amiciziaId}/rifiuta`, {}, { headers: this.creaHeaders() });
  }

  rimuoviAmico(amicoId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/rimuovi/${amicoId}`, { headers: this.creaHeaders() });
  }
}
