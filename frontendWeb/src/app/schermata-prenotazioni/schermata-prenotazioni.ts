import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { Prenotazione } from '../models/prenotazioni.model';
import { PrenotazioneService } from '../service/prenotazione.service';
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

  isLoading: boolean = false;

  constructor(
    private prenotazioneService: PrenotazioneService,
    private authService: AutenticazioneService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = this.authService.ottieniToken();

      if (token) {
        this.caricaPrenotazioniDalDB();
      } else {
        this.isLoading = true;
        setTimeout(() => {
          this.caricaPrenotazioniDalDB();
        }, 1000);
      }
    }
  }

  caricaPrenotazioniDalDB(): void {
    this.isLoading = true;
    this.cdr.detectChanges();

    this.prenotazioneService.getListaPrenotazioni().subscribe({
      next: (rispostaPaginata: any) => {
        this.listaPrenotazioni = rispostaPaginata.content;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (errore) => {
        console.error(errore);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  vaiAlDettaglioViaggio(viaggioId: number) {
    if (this.isLoading) return;
    this.isLoading = true;
    this.router.navigate(['/viaggi', viaggioId]);
  }

  calcolaGiorniMancanti(dataStringa: string | undefined): number {
    if (!dataStringa) return -1;

    const dataPartenza = new Date(dataStringa);
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    dataPartenza.setHours(0, 0, 0, 0);

    const differenzaTempo = dataPartenza.getTime() - oggi.getTime();
    const giorni = Math.round(differenzaTempo / (1000 * 3600 * 24));

    return giorni;
  }
}
