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

import { InboxOrganizzatore } from './inbox-organizzatore/inbox-organizzatore';
import { PaginaInizialeComponent } from './pagina-iniziale/pagina-iniziale';
import { StatisticheOrganizzatore} from './statistiche-organizzatore/statistiche-organizzatore';
import {PrenotaViaggioComponent} from './prenota-viaggio/prenota-viaggio';
import {SezionePagamentoComponent} from './sezione-pagamento/sezione-pagamento';
import {ListaViaggiMarker} from './lista-viaggi-marker/lista-viaggi-marker';
import {InboxViaggiatoreComponent} from './inbox-viaggiatore/inbox-viaggiatore';

export const routes: Routes = [
  {
    path: '',
    component: PaginaInizialeComponent
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
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: 'home',
        component: SchermataHomeComponent,
        canActivate: [autenticazioneGuard]
      },
      // Schermate organizzatore
      {
        path: 'organizzatore',
        component: SchermataOrganizzatoreComponent,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_ORGANIZZATORE', 'ROLE_ADMIN'] }
      },
      {
        path: 'statistiche-organizzatore',
        component: StatisticheOrganizzatore,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_ORGANIZZATORE', 'ROLE_ADMIN'] }
      },
      {
        path: 'inbox-organizzatore',
        component: InboxOrganizzatore,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_ORGANIZZATORE', 'ROLE_ADMIN'] }
      },
      {
        path: 'crea-viaggio',
        component: CreaViaggio,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_ORGANIZZATORE', 'ROLE_ADMIN'] }
      },
      {
        path: 'lista-viaggi-marker',
        component: ListaViaggiMarker,
        canActivate:[autenticazioneGuard],
        data: { ruoli: ['ROLE_ORGANIZZATORE', 'ROLE_ADMIN'] }
      },
      // Schermate viaggiatore
      {
        path: 'inbox-viaggiatore',
        component: InboxViaggiatoreComponent,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_VIAGGIATORE', 'ROLE_ADMIN'] }
      },
      {
        path: 'prenotazioni',
        component: SchermataPrenotazioni,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_VIAGGIATORE', 'ROLE_ADMIN'] }
      },
      {
        path: 'miei-itinerari',
        component: MieiItinerari,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_VIAGGIATORE'] }
      },
      {
        path: 'diventa-organizzatore',
        component: DiventaOrganizzatoreComponent,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_VIAGGIATORE'] }
      },
      {
        path: 'prenota-viaggio/:id',
        component: PrenotaViaggioComponent,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_VIAGGIATORE'] }
      },
      {
        path:'pagamento/:id',
        component:SezionePagamentoComponent,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_VIAGGIATORE'] }
      },
      // Schermate amministratore
      {
        path: 'admin-dashboard',
        component: AdminDashboard,
        canActivate: [autenticazioneGuard],
        data: { ruoli: ['ROLE_ADMIN'] }
      },
      // Schermate aperte a tutti i ruoli
      {
        path: 'viaggi/:id',
        component: DettaglioViaggio,
        canActivate: [autenticazioneGuard]
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
      }
      ]
  },
  //  Il jolly va tassativamente in fondo a tutto, altrimenti intercetta e blocca le rotte sotto di lui
  {
    path: '**',
    redirectTo: ''
  }
];
