import { Routes } from '@angular/router';
import { autenticazioneGuard } from './autenticazione.guard';
import { SchermataHomeComponent } from './schermataHome/schermataHome';
import { SchermataOrganizzatoreComponent } from './schermataOrganizzatore/schermataOrganizzatore';
import { CreaViaggio } from './crea-viaggio/crea-viaggio';
import { Login } from './login/login';
import { Registrazione } from './registrazione/registrazione';
import { SchermataUtente } from './schermata-utente/schermata-utente';
import { SchermataPrenotazioni } from './schermata-prenotazioni/schermata-prenotazioni';
import { AdminDashboard } from './admin-dashboard/admin-dashboard';
import { MieiItinerari } from './miei-itinerari/miei-itinerari';
import { DettaglioViaggio } from './dettaglio-viaggio/dettaglio-viaggio';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { DiventaOrganizzatoreComponent} from './diventa-organizzatore/diventa-organizzatore';

import{ InboxOrganizzatore} from './inbox-organizzatore/inbox-organizzatore';
import {ChatComponent} from './dettaglio-viaggio/components/chat/chat';
import { PaginaInizialeComponent } from './pagina-iniziale/pagina-iniziale';
import { StatisticheOrganizzatore} from './statistiche-organizzatore/statistiche-organizzatore';

export const routes: Routes = [


  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        component: PaginaInizialeComponent
      },
      {
        path: 'home',
        component: SchermataHomeComponent,
        canActivate: [autenticazioneGuard]
      },
      {
        path: 'organizzatore',
        component: SchermataOrganizzatoreComponent,
        canActivate: [autenticazioneGuard]
      },
      {
        path: 'statistiche-organizzatore',
        component: StatisticheOrganizzatore,
        canActivate: [autenticazioneGuard]
      },
      {
        path: 'inbox-organizzatore',
        component: InboxOrganizzatore
      },
      {
        path: 'crea-viaggio',
        component: CreaViaggio
      },
      {
        path: 'profilo',
        component: SchermataUtente,
        canActivate: [autenticazioneGuard]
      },
      {
        path: 'profilo/:id',
        component: SchermataUtente,
        canActivate: [autenticazioneGuard]
      },
      {
        path: 'prenotazioni',
        component: SchermataPrenotazioni
      },
      {
        path: 'admin-dashboard',
        component: AdminDashboard
      },
      {
        path: 'miei-itinerari',
        component: MieiItinerari,
        canActivate: [autenticazioneGuard]
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
      },
      {
        path: 'chat',
        component: ChatComponent

      }
    ];
