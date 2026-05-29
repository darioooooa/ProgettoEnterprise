import { Routes } from '@angular/router';
import { autenticazioneGuard } from './autenticazione.guard';
import { SchermataHomeComponent } from './schermataHome/schermataHome';
import { SchermataOrganizzatoreComponent } from './schermataOrganizzatore/schermataOrganizzatore';
import { CreaViaggio } from './crea-viaggio/crea-viaggio';
import { Login } from './login/login';
import { Registrazione } from './registrazione/registrazione';
import { SchermataUtente } from './schermata-utente/schermata-utente';
import { SchermataPrenotazioni } from './schermata-prenotazioni/schermata-prenotazioni';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard';
import { MieiItinerari } from './miei-itinerari/miei-itinerari';
import { ListaTappe } from './lista-tappe/lista-tappe';
import { DettaglioViaggio } from './dettaglio-viaggio/dettaglio-viaggio';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { DiventaOrganizzatoreComponent} from './diventa-organizzatore/diventa-organizzatore';

export const routes: Routes = [


  {
    path: '',
    component: MainLayoutComponent,
    children: [
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
        path: 'miei-itinerari',
        component: MieiItinerari,
        canActivate: [autenticazioneGuard]
      },
      {

        path: 'lista-tappe/:id',
        component: ListaTappe
      },
      {
        path: 'viaggi/:id',
        component: DettaglioViaggio,
        canActivate: [autenticazioneGuard]
      },
      {
        path: 'diventa-organizzatore',
        component: DiventaOrganizzatoreComponent

      }
    ]
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
    path: '**',
    redirectTo: ''
  }
];
