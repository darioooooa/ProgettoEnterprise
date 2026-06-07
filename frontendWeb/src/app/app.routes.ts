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
import {ListaViaggiMarker} from './lista-viaggi-marker/lista-viaggi-marker';
import{ InboxOrganizzatore} from './inbox-organizzatore/inbox-organizzatore';
import {ChatComponent} from './dettaglio-viaggio/components/chat/chat';


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
      { path: 'inbox-organizzatore',
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

      },
      {
        path:'lista-viaggi-marker',
        component:ListaViaggiMarker
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
    path: 'inbox-organizzatore',
    component: InboxOrganizzatore,
  },
  {
    path:'chat',
    component: ChatComponent

  }
];
