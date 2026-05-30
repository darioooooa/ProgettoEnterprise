import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UtenteService {
  private readonly backendUrl = 'http://localhost:8080/api/v1/utenti';

  constructor(private http: HttpClient) {}

  getProfiloById(id: number): Observable<any> {
    return this.http.get<any>(`${this.backendUrl}/${id}`);
  }
}
