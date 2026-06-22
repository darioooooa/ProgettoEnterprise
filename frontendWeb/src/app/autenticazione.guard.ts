import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AutenticazioneService } from './service/autenticazione.service';

export const autenticazioneGuard: CanActivateFn = (route, state) => {
  const servAuth = inject(AutenticazioneService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  // Reindirizzamento in base al ruolo dell'utente
  // Se l'utente forza la rotta da URL, lo si rispedisce alla sua specifica home
  const eseguiRedirectInBaseAlRuolo = (ruolo: string | null) => {
    switch (ruolo) {
      case 'ROLE_ORGANIZZATORE':
        router.navigate(['/organizzatore']);
        break;
      case 'ROLE_ADMIN':
        router.navigate(['/admin-dashboard']);
        break;
      case 'ROLE_VIAGGIATORE':
      default:
        router.navigate(['/home']);
        break;
    }
  };

  // Verifica sul ruolo dell'utente
  const verificaPermessoRuolo = (): boolean => {
    // Recupera l'array dei ruoli accettati per la rotta corrente (definito in app.routes.ts)
    const ruoliAmmessi = route.data?.['ruoli'] as string[];
    const ruoloUtente = servAuth.ottieniRuolo(); // 'ROLE_VIAGGIATORE', 'ROLE_ORGANIZZATORE', 'ROLE_ADMIN'

    // Se la rotta non richiede ruoli specifici, lo si fa passare
    if (!ruoliAmmessi || ruoliAmmessi.length === 0) {
      return true;
    }

    // Se l'utente ha uno dei ruoli consentiti, l'accesso è approvato
    if (ruoloUtente && ruoliAmmessi.includes(ruoloUtente)) {
      return true;
    }

    // Se il ruolo non coincide, si blocca l'accesso e si attiva il reindirizzamento
    console.warn(`Accesso negato per il ruolo ${ruoloUtente} sulla rotta: ${state.url}`);
    eseguiRedirectInBaseAlRuolo(ruoloUtente);
    return false;
  };

  // Controllo principale: autenticazione standard + refresh token)
  if (servAuth.isLoggato()) {
    return verificaPermessoRuolo(); // Se è loggato, esegue il controllo del ruolo
  }

  const documentoDiRiserva = servAuth.ottieniRefreshToken();

  if (documentoDiRiserva) {
    console.log('Documento principale scaduto, provo a chiederne uno nuovo...');

    return servAuth.eseguiRefresh().pipe(
      map(() => {
        // Se il refresh va a buon fine, effettua comunque il controllo del ruolo
        return verificaPermessoRuolo();
      }),
      catchError(() => {
        console.log('Rinnovo fallito, torno al login.');
        router.navigate(['/login']);
        return of(false);
      })
    );
  }

  router.navigate(['/login']);
  return false;
};
