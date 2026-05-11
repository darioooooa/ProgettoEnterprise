import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterLink} from '@angular/router';
import {AutenticazioneService} from '../service/autenticazione.service';

@Component({
  selector: 'app-schermata-home', // Il nome del "tag" HTML
  standalone: true,              // Indica che il componente si gestisce da solo
  imports: [CommonModule, RouterLink],
  templateUrl: './schermataHome.html',
  styleUrl: './schermataHome.css'
})
export class SchermataHomeComponent {
  mostraMenu=false
  titolo = 'Benvenuti in TravelBooking';


  viaggiEsempio = [
    { destinazione: 'Roma', descrizione: 'Tour del Colosseo', prezzo: 45 },
    { destinazione: 'Parigi', descrizione: 'Crociera sulla Senna', prezzo: 60 }
  ];

  constructor(private servAuth: AutenticazioneService) {
    console.log('Schermata Home inizializzata!');
  }
  isLoggato(): boolean {
    return this.servAuth.isLoggato();
  }
  toggleMenu(){
    return this.mostraMenu=!this.mostraMenu
  }
  logout(){
    this.servAuth.esci();
  }
  ottieniUsername(): string | null{
    return this.servAuth.ottieniUsername();
  }

}
