import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {RichiestaPromozione} from '../admin-dashboard/admin-dashboard';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private apiUrl = 'http://localhost:8080/api/admin/richieste';

  constructor(private http: HttpClient) {}


  getRichieste(): Observable<RichiestaPromozione[]> {
    return this.http.get<RichiestaPromozione[]>(this.apiUrl);
  }

  approvaRichiesta(id: number,idAdmin: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/approva`, {}, { responseType: 'text' });
    return this.http.post(`${this.apiUrl}/${id}/approva?adminIdCorrente=${idAdmin}`, {});
  }


  rifiutaRichiesta(id: number, note: string, adminId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/rifiuta`, { noteAdmin: note }, { responseType: 'text' })
    const payload = { note: note };
    return this.http.post(`${this.apiUrl}/${id}/rifiuta?adminIdCorrente=${adminId}`, payload)
  }
}
