import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AutenticazioneService } from './service/autenticazione.service';
import {environment} from '../environments/development';

// Si mette in mezzo tra angular e il server
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private staAggiornandoToken = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(private authService: AutenticazioneService,
              private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Aggiunge il token iniziale alla richiesta
    let richiestaAutenticata = this.aggiungiToken(request, this.authService.ottieniToken());

    // Inoltra la richiesta a Spring Boot
    return next.handle(richiestaAutenticata).pipe(
      catchError((errore: HttpErrorResponse) => {

        // Gestione del rate limiting (429)
        if (errore.status === 429) {
          const secondiAttesa = errore.headers.get('X-Rate-Limit-Retry-After-Seconds') || 'fallback';

          alert(`⚠️ OPERAZIONE BLOCCATA (Rate Limit)\nHai inviato troppe richieste troppo velocemente.\nRiprova tra ${secondiAttesa} secondi.`);

          return throwError(() => errore);
        }

        // Se il token è scaduto
        if (errore.status === 401 && !request.url.includes('protocol/openid-connect/token')) {
          return this.gestisciErrore401(request, next);
        }

        // Se una richiesta fallisce direttamente con 403 o 401 non gestito sulla sessione iniziale
        if ((errore.status === 401 || errore.status === 403) && !this.authService.ottieniToken()) {
          this.eseguiResetInizialeSessioneOrfana();
        }

        if (errore.status === 403 && errore.error?.errore?.includes('sospeso')) {
          alert("⚠️ IL TUO ACCOUNT È STATO SOSPESO.\nSarai disconnesso immediatamente.");
          this.authService.esci();
          this.router.navigate(['/login']);
        }

        // Se è un altro errore (ad esempio 403 Forbidden, 404 Not Found) lo fa passare
        return throwError(() => errore);
      })
    );
  }

  // Metodo per clonare la richiesta in modo pulito
  private aggiungiToken(request: HttpRequest<any>, token: string | null): HttpRequest<any> {


    const isRottaBackend = request.url.includes('/api/') ||
      request.url.includes(environment.CURRENT_IP) ||
      request.url.includes('localhost');

    if (token && isRottaBackend) {
      return request.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return request;
  }

  // Gestione dell'errore e del refresh del token
  private gestisciErrore401(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // Se nessuno sta aggiornando il token
    if (!this.staAggiornandoToken) {
      this.staAggiornandoToken = true;
      this.refreshTokenSubject.next(null);

      return this.authService.eseguiRefresh().pipe(
        switchMap((nuoviToken: any) => {
          this.staAggiornandoToken = false;
          // Il nuovo token
          this.refreshTokenSubject.next(nuoviToken.access_token);

          // Ritenta la richiesta originale che era fallita, ma col token nuovo
          return next.handle(this.aggiungiToken(request, nuoviToken.access_token));
        }),
        catchError((erroreRefresh) => {
          // Anche il refresh token è scaduto (o è stato revocato)
          this.staAggiornandoToken = false;
          this.eseguiResetInizialeSessioneOrfana();
          return throwError(() => erroreRefresh);
        })
      );
    } else {
      // Se qualcuno sta già aggiornando il token aspetta
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1), // Prende il token e si cancella dalla coda
        switchMap(token => {
          // Ritenta la richiesta originale col nuovo token
          return next.handle(this.aggiungiToken(request, token));
        })
      );
    }
  }

  // Metodo per ripristinare l'intera sessione iniziale (logout)
  private eseguiResetInizialeSessioneOrfana(): void {
    console.warn("Rilevato token orfano o scaduto irreversibilmente. Ripristino dell'applicazione.");

    // Pulisce tutte le strutture di tracciamento
    localStorage.clear();
    sessionStorage.clear();

    // Distrugge lo stato in memoria del servizio
    this.authService.esci();

    this.router.navigate(['/']).then(() => {
      window.location.reload();
    });
  }
}
