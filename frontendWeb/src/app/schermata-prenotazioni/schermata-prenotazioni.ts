import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core'; // 1. AGGIUNGI ChangeDetectorRef QUI
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Prenotazione } from '../models/prenotazioni.model';
import { PrenotazioneService } from '../service/prenotazioni.service';
import { AutenticazioneService } from '../service/autenticazione.service';

@Component({
  selector: 'app-schermata-prenotazioni',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schermata-prenotazioni.html',
  styleUrl: './schermata-prenotazioni.css'
})
export class SchermataPrenotazioni implements OnInit {

  listaPrenotazioni: Prenotazione[] = [];

  constructor(
    private prenotazioneService: PrenotazioneService,
    private authService: AutenticazioneService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = this.authService.ottieniToken();

      if (token) {
        this.caricaPrenotazioniDalDB();
      } else {
        console.log('Token non ancora pronto nel browser, aspetto il caricamento...');
        setTimeout(() => {
          this.caricaPrenotazioniDalDB();
        }, 1000);
      }
    }
  }

  caricaPrenotazioniDalDB(): void {
    this.prenotazioneService.getListaPrenotazioni().subscribe({
      next: (rispostaPaginata: any) => {
        this.listaPrenotazioni = rispostaPaginata.content;
        console.log('Prenotazioni reali caricate:', this.listaPrenotazioni);

        // 3. FORZA IL DISEGNO DELL'HTML APPENA ARRIVANO I DATI
        this.cdr.detectChanges();
      },
      error: (errore) => {
        console.error('Errore nel recupero delle prenotazioni:', errore);
      }
    });
  }
}
