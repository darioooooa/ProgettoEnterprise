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
  getViaggi(filter?: any): Observable<Viaggio[]> {
    let params = new HttpParams();

    if (filter) {
      Object.keys(filter).forEach(key => {
        if (filter[key] !== null && filter[key] !== undefined) {
          params = params.append(key, filter[key]);
        }
      })
    }

    return this.http.get<Viaggio[]>(this.API_URL, { params } );
  }
}
