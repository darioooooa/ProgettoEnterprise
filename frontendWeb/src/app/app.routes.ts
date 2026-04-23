import { Routes } from '@angular/router';
import { SchermataHomeComponent } from './schermataHome/schermataHome';
import {SchermataOrganizzatoreComponent} from './schermataOrganizzatore/schermataOrganizzatore';

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
  }
];
