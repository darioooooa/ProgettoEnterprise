import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AmiciziaService {

  private readonly baseUrl = 'http://localhost:8080/api/v1/amicizie';

  constructor(private http: HttpClient) { }

  inviaRichiesta(riceventeUsername: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/richiesta/${riceventeUsername}`, {});
  }

  accettaRichiesta(amiciziaId: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/${amiciziaId}/accetta`, {});
  }

  ottieniMieiAmici(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/miei-amici`);
  }

  ottieniRichiesteRicevute(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/richieste/ricevute`);
  }

  ottieniRichiesteInviate(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/richieste/inviate`);
  }

  ottieniRichiesteRifiutate(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/richieste/rifiutate`);
  }

  rifiutaRichiesta(amiciziaId: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/${amiciziaId}/rifiuta`, {});
  }

  rimuoviAmico(amicoId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/rimuovi/${amicoId}`);
  }
}
