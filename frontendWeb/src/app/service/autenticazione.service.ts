
import {Inject, Injectable, PLATFORM_ID} from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {isPlatformBrowser} from '@angular/common';


@Injectable({
  providedIn: 'root'
})
export class AutenticazioneService {


  private readonly indirizzoBase = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient,@Inject(PLATFORM_ID)private platformId: Object) { }

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
    if(isPlatformBrowser(this.platformId)){
      return localStorage.getItem('token_accesso');
    }
    return null;
  }


  isLoggato(): boolean {
    return !!this.ottieniToken();
  }
}
