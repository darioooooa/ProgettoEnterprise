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

  listaPrenotazioni: any[] = [];
  isLoading: boolean = false;
  tabAttivo: 'attivi' | 'storico' = 'attivi';

  paginaCorrente: number = 0;
  totalePagine: number = 0;

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

  caricaPrenotazioniDalDB(pagina: number = 0): void {
    this.isLoading = true;
    this.paginaCorrente = pagina;
    this.cdr.detectChanges();

    this.prenotazioneService.getListaPrenotazioni(this.paginaCorrente, {}).subscribe({
      next: (rispostaPaginata: any) => {
        this.listaPrenotazioni = rispostaPaginata.content || [];
        this.totalePagine = rispostaPaginata.totalPages || 0;
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

  paginaPrecedente() {
    if (this.paginaCorrente > 0 && !this.isLoading) {
      this.caricaPrenotazioniDalDB(this.paginaCorrente - 1);
    }
  }

  paginaSuccessiva() {
    if (this.paginaCorrente < this.totalePagine - 1 && !this.isLoading) {
      this.caricaPrenotazioniDalDB(this.paginaCorrente + 1);
    }
  }

  // Filtra i viaggi in programma o attualmente in corso
  get prenotazioniAttive(): any[] {
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    return this.listaPrenotazioni.filter(p => {
      if (!p.viaggioDataFine) return true; // Fallback di sicurezza
      const fineViaggio = new Date(p.viaggioDataFine);
      fineViaggio.setHours(0, 0, 0, 0);
      return fineViaggio >= oggi;
    });
  }

  // Filtra i viaggi storici interamente completati
  get prenotazioniPassate(): any[] {
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    return this.listaPrenotazioni.filter(p => {
      if (!p.viaggioDataFine) return false;
      const fineViaggio = new Date(p.viaggioDataFine);
      fineViaggio.setHours(0, 0, 0, 0);
      return fineViaggio < oggi;
    });
  }

  // Calcola lo stato temporale del viaggio per inserire un testo descrittivo
  getTestoStatoViaggio(dataInizio: string, dataFine: string): { testo: string, classe: string } {
    if (!dataInizio || !dataFine) return { testo: 'Confermato', classe: 'badge-grigio' };

    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    const inizio = new Date(dataInizio);
    const fine = new Date(dataFine);

    if (oggi < inizio) {
      return { testo: '📅 In Programma', classe: 'badge-futuro' };
    } else if (oggi >= inizio && oggi <= fine) {
      return { testo: '✈️ In Corso', classe: 'badge-in-corso' };
    } else {
      return { testo: '🏁 Completato', classe: 'badge-completato' };
    }
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

  annullaPrenotazione(evento: Event, idPrenotazione: number): void {
    evento.stopPropagation();

    const conferma = window.confirm('Sei sicuro di voler annullare questa prenotazione?');

    if (conferma) {
      this.prenotazioneService.cancellaPrenotazione(idPrenotazione).subscribe({
        next: () => {
          alert('Prenotazione annullata con successo!');
          this.caricaPrenotazioniDalDB();
        },
        error: (errore) => {
          console.error(errore);
          alert("Si è verificato un problema durante l'annullamento.");
        }
      });
    }
  }

}
