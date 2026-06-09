import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, throwError, switchMap, map } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AutenticazioneService {

  // Endpoint di keycloak per prendere il token
  private readonly keycloakTokenUrl = 'http://localhost:8081/realms/enterprise-realm/protocol/openid-connect/token';
  // Endpoint per il backend
  private readonly backendUrl = '/api/v1';

  private clientId = 'enterprise-client';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  effettuaAccesso(datiLogin: any): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });

    const body = new URLSearchParams();
    body.set('grant_type', 'password');
    body.set('client_id', 'enterprise-client');
    body.set('username', datiLogin.username);
    body.set('password', datiLogin.password);


    return this.http.post<any>(this.keycloakTokenUrl, body.toString(), { headers }).pipe(
      tap(rispostaKeycloak => {
        if (rispostaKeycloak && rispostaKeycloak.access_token) {

          const token = rispostaKeycloak.access_token;
          const refreshToken = rispostaKeycloak.refresh_token;

          localStorage.setItem('token_accesso', token);
          if (rispostaKeycloak.refresh_token) {
            localStorage.setItem('token_refresh', refreshToken);
          }

          // Decodifica il JWT per estrarre i dati dell'utente
          const payloadCodificato = token.split('.')[1];
          const payloadDecodificato = JSON.parse(window.atob(payloadCodificato));

          // Salva le informazioni estratte da Keycloak
          localStorage.setItem('username', payloadDecodificato.preferred_username);
          localStorage.setItem('email', payloadDecodificato.email);
          localStorage.setItem('nome', payloadDecodificato.given_name);
          localStorage.setItem('cognome', payloadDecodificato.family_name);

          // Estrae il ruolo
          const ruoliKeycloak = payloadDecodificato.realm_access?.roles || [];
          let ruoloPrincipale = 'ROLE_VIAGGIATORE'; // Default

          if (ruoliKeycloak.includes('ADMIN')) {
            ruoloPrincipale = 'ROLE_ADMIN';
          } else if (ruoliKeycloak.includes('ORGANIZZATORE')) {
            ruoloPrincipale = 'ROLE_ORGANIZZATORE';
          }

          localStorage.setItem('ruolo', ruoloPrincipale);
        }
      }),

      switchMap(rispostaKeycloak => {
        if (rispostaKeycloak && rispostaKeycloak.access_token) {
          const token = rispostaKeycloak.access_token;

          // Impostazione dell'header manualmente visto che l'interceptor potrebbe non essere ancora pronto durante il login
          const authHeaders = new HttpHeaders({
            'Authorization': `Bearer ${token}`
          });

          return this.http.get<any>(`${this.backendUrl}/utenti/me`, { headers: authHeaders }).pipe(
            tap(utenteDatabase => {
              if (utenteDatabase && utenteDatabase.id) {
                localStorage.setItem('userId', utenteDatabase.id.toString());
              }
            }),
            map(() => rispostaKeycloak)
          );
        }
        return throwError(() => new Error("Impossibile recuperare il token di accesso"));
      })
    );
  }

  eseguiRefresh(): Observable<any> {
    const refreshToken = this.ottieniRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error("Nessun refresh token disponibile."));
    }
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });

    const body = new URLSearchParams();
    body.set('grant_type', 'refresh_token');
    body.set('client_id', this.clientId);
    body.set('refresh_token', refreshToken);

    return this.http.post<any>(this.keycloakTokenUrl, body.toString(), { headers }).pipe(
      tap((rispostaKeycloak: any) => {
        if (rispostaKeycloak && rispostaKeycloak.access_token) {
          // Aggiorna i nuovi token
          localStorage.setItem('token_accesso', rispostaKeycloak.access_token);
          if (rispostaKeycloak.refresh_token) {
            localStorage.setItem('token_refresh', rispostaKeycloak.refresh_token);
          }
        }
      })
    );
  }

  esci(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.clear(); // Svuota tutto
    }
  }

  registraNuovoUtente(datiRegistrazione: any): Observable<any> {
    return this.http.post<any>(`${this.backendUrl}/auth/register`, datiRegistrazione);
  }

  ottieniToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token_accesso');
    }
    return null;
  }

  ottieniRefreshToken(): string | null{
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token_refresh');
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

  ottieniRuolo(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('ruolo');
    }
    return null;
  }

  ottieniId(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('userId');
    }
    return null;
  }

  isLoggato(): boolean {
    return !!this.ottieniToken();
  }

  // Ricerca Utenti
  ottieniDatiUtenteDalDatabase(username: string): Observable<any> {
    return this.http.get<any>(`${this.backendUrl}/viaggiatori/cerca?query=${username}`);
  }

  inviaRichiestaPromozione(viaggiatoreId: number, datiRichiesta: any): Observable<any> {
    const url = `${this.backendUrl}/viaggiatori/richieste-promozione`;

    return this.http.post(url, datiRichiesta, { responseType: 'text' });
  }}
