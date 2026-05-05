
import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class AutenticazioneService {


  private readonly indirizzoBase = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient) { }

  effettuaAccesso(datiLogin: any): Observable<any> {
    return this.http.post<any>(`${this.indirizzoBase}/login`, datiLogin).pipe(
      tap(risposta => {

        if (risposta && risposta.token) {
          localStorage.setItem('token_accesso', risposta.token);
        }
      })
    );
  }


  esci(): void {

    this.http.post(`${this.indirizzoBase}/logout`, {}).subscribe();


    localStorage.removeItem('token_accesso');
  }

  registraNuovoUtente(datiRegistrazione: any): Observable<any> {
    return this.http.post<any>(`${this.indirizzoBase}/register`, datiRegistrazione);
  }


  ottieniToken(): string | null {
    return localStorage.getItem('token_accesso');
  }


  isLoggato(): boolean {
    return !!this.ottieniToken();
  }
}
