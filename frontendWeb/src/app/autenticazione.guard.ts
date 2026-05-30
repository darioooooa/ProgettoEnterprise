import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AutenticazioneService } from './service//autenticazione.service';

export const autenticazioneGuard: CanActivateFn = (route, state) => {
  const servAuth = inject(AutenticazioneService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID); // 🚀 Inietta l'ID della piattaforma

  if (!isPlatformBrowser(platformId)) {
    return true;
  }
  if (servAuth.isLoggato()) {
    return true;
  }

  const documentoDiRiserva = servAuth.ottieniRefreshToken();

  if (documentoDiRiserva) {
    console.log('Documento principale scaduto, provo a chiederne uno nuovo...');

    return servAuth.eseguiRefresh().pipe(
      map(() => {
        return true;
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
