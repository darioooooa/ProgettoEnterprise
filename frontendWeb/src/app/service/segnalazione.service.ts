import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SegnalazioneService {

  private urlBase = '/api/v1/segnalazioni';

  constructor(private http: HttpClient) {}

  creaSegnalazione(datiSegnalazione: any, idSegnalatore: number): Observable<any> {
    return this.http.post(`${this.urlBase}/crea?idSegnalatore=${idSegnalatore}`, datiSegnalazione);
  }

  cercaSegnalazioni(filtro: any, pagina: number, dimensione: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('dimensione', dimensione.toString());

    // Aggiungi i filtri solo se esistono
    if (filtro.tipo) {
      params = params.set('tipo', filtro.tipo);
    }
    if (filtro.stato) {
      params = params.set('stato', filtro.stato);
    }
    if (filtro.segnalatoreId) {
      params = params.set('segnalatoreId', filtro.segnalatoreId.toString());
    }
    if (filtro.adminId) {
      params = params.set('adminId', filtro.adminId.toString());
    }

    return this.http.get<any>(`${this.urlBase}/ricerca`, { params });
  }
  prendiInCarico(idSegnalazione: number, idAdmin: number): Observable<any> {
    return this.http.put(`${this.urlBase}/${idSegnalazione}/prendi-in-carico?idAdmin=${idAdmin}`, {});
  }

  risolviSegnalazione(idSegnalazione: number, idAdmin: number, sospendiAutore: boolean = false): Observable<any> {
    return this.http.put(`${this.urlBase}/${idSegnalazione}/risolvi?idAdmin=${idAdmin}&sospendiAutore=${sospendiAutore}`, {});
  }

  rifiutaSegnalazione(idSegnalazione: number, idAdmin: number): Observable<any> {
    return this.http.put(`${this.urlBase}/${idSegnalazione}/rifiuta?idAdmin=${idAdmin}`, {});
  }

  contaSegnalazioniAperte(): Observable<number> {
    return this.http.get<number>(`${this.urlBase}/contatore-aperte`);
  }
}
