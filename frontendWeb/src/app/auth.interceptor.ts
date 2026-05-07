import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AutenticazioneService } from './service/autenticazione.service'; // Controlla il percorso!

//si mette in mezzo tra angular e il server. Ogni volta che faccio una get o una post
//aggiungo l'header Authorization con il token ed evita ripetizioni di codice
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AutenticazioneService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Recuperiamo il token usando il metodo che ho scritto nel service
    const token = this.authService.ottieniToken();

    // Se il token esiste aggiungo l'header Authorization
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // Invio la richiesta al server
    return next.handle(request);
  }
}
