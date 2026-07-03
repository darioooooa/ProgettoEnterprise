import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {RichiestaPromozione} from '../admin-dashboard/admin-dashboard';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private apiUrl = '/api/v1/admin/richieste';

  constructor(private http: HttpClient) {}

  getRichiestePaginate(page: number = 0, size: number = 10, stato?: string, username?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (stato) {
      params = params.set('stato', stato);
    }

    if (username && username.trim()) {
      params = params.set('username', username.trim());
    }

    return this.http.get<any>(this.apiUrl, { params });
  }

  getRichieste(): Observable<RichiestaPromozione[]> {
    return this.http.get<RichiestaPromozione[]>(this.apiUrl);
  }

  approvaRichiesta(id: number, adminId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/approva`, {}, { responseType: 'text' });
  }

  rifiutaRichiesta(id: number, note: string, adminId: number): Observable<any> {
    const payload = { noteAdmin: note };
    return this.http.post(`${this.apiUrl}/${id}/rifiuta`, payload, { responseType: 'text' });
  }

  getUtentiBannati(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/admin/richieste/utenti-bannati');
  }

  sbannaUtente(idUtente: number): Observable<any> {
    return this.http.put(`/api/v1/admin/richieste/utenti/${idUtente}/riattiva`, {});
  }

  scaricaDocumento(idRichiesta: number) {
    return this.http.get(`/api/v1/admin/richieste/promozioni/${idRichiesta}/documento`, {
      responseType: 'blob'
    });
  }
}
