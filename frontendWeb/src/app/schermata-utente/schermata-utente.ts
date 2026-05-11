import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';

@Component({
  selector: 'app-schermata-utente',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './schermata-utente.html',
  styleUrl: './schermata-utente.css'
})
export class SchermataUtente {


  constructor(
    private servizioAutenticazione: AutenticazioneService,
    @Inject(PLATFORM_ID) private idPiattaforma: Object
  ) {}

  ottieniNome(): string | null{
    return this.servizioAutenticazione.ottieniNome();
  }
  ottieniCognome(): string | null{
    return this.servizioAutenticazione.ottieniCognome();
  }
  ottieniEmail(): string | null{
    return this.servizioAutenticazione.ottieniEmail();
  }
  ottieniUsername(): string | null{
    return this.servizioAutenticazione.ottieniUsername();
  }
}
