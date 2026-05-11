import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AutenticazioneService {

  // Endpoint per login e registrazione
  private readonly indirizzoAuth = 'http://localhost:8080/api/v1/auth';
  // Endpoint per recuperare i dati del profilo (regolalo in base al tuo Controller Java)
  private readonly indirizzoUtente = 'http://localhost:8080/api/v1/utenti';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }


  ottieniDatiUtenteDalDatabase(username: string): Observable<any> {
    const token = this.ottieniToken();


    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<any>(`${this.indirizzoUtente}/${username}`, { headers });
  }


  effettuaAccesso(datiLogin: any): Observable<any> {
    return this.http.post<any>(`${this.indirizzoAuth}/login`, datiLogin).pipe(
      tap(risposta => {
        if (risposta && risposta.token) {
          localStorage.setItem('token_accesso', risposta.token);
          localStorage.setItem('username', risposta.username);
          localStorage.setItem('nome', risposta.nome);
          localStorage.setItem('cognome', risposta.cognome);
          localStorage.setItem('email', risposta.email);

        }
      })
    );
  }

  esci(): void {
    // Il logout lato server è opzionale a seconda di come hai gestito JWT
    this.http.post(`${this.indirizzoAuth}/logout`, {}).subscribe();

    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token_accesso');
      localStorage.removeItem('username');
      localStorage.removeItem('nome');
      localStorage.removeItem('cognome');
      localStorage.removeItem('email');
    }
  }

  registraNuovoUtente(datiRegistrazione: any): Observable<any> {
    return this.http.post<any>(`${this.indirizzoAuth}/register`, datiRegistrazione);
  }

  ottieniToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token_accesso');
    }
    return null;
  }

  ottieniUsername(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('username');
    }
    return null;
  }
  ottieniNome(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('nome');
    }
    return null;
  }
  ottieniCognome(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('cognome');
    }
    return null;
  }
  ottieniEmail(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('email');
    }
    return null;
  }

  isLoggato(): boolean {
    return !!this.ottieniToken();
  }
}
