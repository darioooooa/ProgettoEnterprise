import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';
import { Viaggio } from '../models/viaggio.model';

@Injectable({
  providedIn: 'root'
})
export class ViaggioService {
  private readonly API_URL = 'http://localhost:8080/api/v1/viaggi';

  constructor(private http: HttpClient) { }

  creaViaggio(viaggio: Viaggio): Observable<Viaggio> {

    return this.http.post<Viaggio>(this.API_URL, viaggio);
  }
  getViaggi(page: number = 0, filter?: any): Observable<any> {
    let params = new HttpParams().set('page', page.toString());

    if (filter) {
      Object.keys(filter).forEach(key => {
        if (filter[key] !== null && filter[key] !== undefined) {
          params = params.append(key, filter[key]);
        }
      })
    }

    return this.http.get<any>(this.API_URL, { params } );
  }
  getViaggiPerMappa(): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/mappa-viaggi`);
  }
}
