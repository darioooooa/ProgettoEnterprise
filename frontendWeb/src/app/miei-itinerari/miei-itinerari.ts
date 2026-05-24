import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ItinerarioService } from '../service/itinerario.service';
import { AutenticazioneService } from '../service/autenticazione.service';
@Component({
  selector: 'app-miei-itinerari',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './miei-itinerari.html',
  styleUrl: './miei-itinerari.css'
})
export class MieiItinerari implements OnInit {
  public itinerari: any[] = [];

  constructor(
    private itinerarioService: ItinerarioService,
    private cdr: ChangeDetectorRef,
    private authService: AutenticazioneService,
    private router: Router
  ) {}

  ngOnInit() {
    console.log("TOKEN:", this.authService.ottieniToken());
    console.log("LOGGATO:", this.authService.isLoggato());
    if (!this.authService.isLoggato()) {
      console.log("Accesso negato: utente sconosciuto.");

      this.router.navigate(['/login']);
      return;
    }

    this.caricaItinerari();
  }

  caricaItinerari() {
    this.itinerarioService.getMieListe().subscribe({
      next: (data) => {
        this.itinerari = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Non è stato possibile caricare gli itinerari", err);
      }
    });
  }
}
