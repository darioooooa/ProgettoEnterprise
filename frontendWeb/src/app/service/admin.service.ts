import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {RichiestaPromozione} from '../admin-dashboard/admin-dashboard';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private apiUrl = '/api/admin/richieste';

  constructor(private http: HttpClient) {}


  getRichieste(): Observable<RichiestaPromozione[]> {
    return this.http.get<RichiestaPromozione[]>(this.apiUrl);
  }

  approvaRichiesta(id: number, adminId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/approva?adminIdCorrente=${adminId}`, {}, { responseType: 'text' });
  }

  rifiutaRichiesta(id: number, note: string, adminId: number): Observable<any> {
    const payload = { noteAdmin: note };
    return this.http.post(`${this.apiUrl}/${id}/rifiuta?adminIdCorrente=${adminId}`, payload, { responseType: 'text' });
  }
  getUtentiBannati(): Observable<any[]> {
    return this.http.get<any[]>('/api/admin/richieste/utenti-bannati');
  }

  sbannaUtente(idUtente: number): Observable<any> {
    return this.http.put(`/api/admin/richieste/utenti/${idUtente}/riattiva`, {});
  }
  scaricaDocumento(idRichiesta: number) {
    return this.http.get(`/api/admin/richieste/promozioni/${idRichiesta}/documento`, {
      responseType: 'blob'
    });
  }
}
