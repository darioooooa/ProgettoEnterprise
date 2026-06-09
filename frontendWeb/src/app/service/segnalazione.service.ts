import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SegnalazioneService {

  private urlBase = '/api/segnalazioni';

  constructor(private http: HttpClient) {}

  creaSegnalazione(datiSegnalazione: any, idSegnalatore: number): Observable<any> {
    return this.http.post(`${this.urlBase}/crea?idSegnalatore=${idSegnalatore}`, datiSegnalazione);
  }

  cercaSegnalazioni(filtri: any, pagina: number = 0): Observable<any> {
    let parametri = new HttpParams().set('pagina', pagina.toString());

    if (filtri.tipo) parametri = parametri.set('tipo', filtri.tipo);
    if (filtri.stato) parametri = parametri.set('stato', filtri.stato);
    if (filtri.segnalatoreId) parametri = parametri.set('segnalatoreId', filtri.segnalatoreId.toString());
    if (filtri.adminId) parametri = parametri.set('adminId', filtri.adminId.toString());

    return this.http.get(`${this.urlBase}/ricerca`, { params: parametri });
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
