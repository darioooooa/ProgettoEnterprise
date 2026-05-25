import { Routes } from '@angular/router';
import { autenticazioneGuard } from './autenticazione.guard';
import { SchermataHomeComponent } from './schermataHome/schermataHome';
import {SchermataOrganizzatoreComponent} from './schermataOrganizzatore/schermataOrganizzatore';
import {CreaViaggio} from './crea-viaggio/crea-viaggio';
import {Login} from './login/login';
import {Registrazione} from './registrazione/registrazione';
import{SchermataUtente} from './schermata-utente/schermata-utente';
import{SchermataPrenotazioni} from './schermata-prenotazioni/schermata-prenotazioni';
import {AdminDashboardComponent} from './admin-dashboard/admin-dashboard';
import{MieiItinerari} from './miei-itinerari/miei-itinerari';
import {DettagliViaggio} from './dettagli-viaggio/dettagli-viaggio';

export const routes: Routes = [
  {
    path: '',
    component: SchermataHomeComponent
  },
  {
    path: 'home',
    component: SchermataHomeComponent
  },
  {
    path: 'organizzatore',
    component: SchermataOrganizzatoreComponent
  },
  {
    path: 'crea-viaggio',
    component: CreaViaggio
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: 'registrazione',
    component: Registrazione
  },
  {
    path: 'profilo',
    component: SchermataUtente
  },
  {
    path: 'prenotazioni',
    component: SchermataPrenotazioni

  },
  {
    path: 'admin-dashboard',
    component: AdminDashboardComponent
  },
  {
    path:'miei-itinerari',
    component: MieiItinerari,
    canActivate: [autenticazioneGuard]
  },
  {
    path:'dettagli-viaggio/:id',
    component: DettagliViaggio,
    canActivate: [autenticazioneGuard]
  }

];
