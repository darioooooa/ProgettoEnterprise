import { Component, OnInit, ChangeDetectorRef, NgZone, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutenticazioneService } from '../service/autenticazione.service';
import { ViaggioService } from '../service/viaggio.service';
import { ItinerarioService } from '../service/itinerario.service';

@Component({
  selector: 'app-schermata-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './schermataHome.html',
  styleUrl: './schermataHome.css'
})
export class SchermataHomeComponent implements OnInit {
  titolo = 'Benvenuti in TravelBooking';

  mostraAvanzati: boolean = false;

  filtriViaggio = {
    destinazione: '',
    dataInizioMin: '',
    dataFineMax: '',
    maxPartecipanti: '',
    prezzoMin: '0',
    prezzoMax: '10000'
  };
  viaggiTrovati: any[] = [];
  haCercato = false;

  paginaCorrente = 0;
  totalePagine = 0;

  openItineraryMenuId: number | null = null;
  mieiItinerari: any[] = [];
  nomeNuovoItinerario: string = ''; // Variabile per l'input del nuovo itinerario

  mioUsername: string = '';
  modaleItinerarioAperta = false;

  isLoading: boolean = false;

  constructor(
    private servAuth: AutenticazioneService,
    private cdr: ChangeDetectorRef,
    private viaggioService: ViaggioService,
    private zone: NgZone,
    private itinerarioService: ItinerarioService,
    private navigatore: Router
  ) {
    console.log('Schermata Home inizializzata!');
  }

  ngOnInit() {

    this.mioUsername = this.servAuth.ottieniUsername() || 'Utente';

    if (this.isLoggato()) {
      this.caricaItinerariUtente();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (this.openItineraryMenuId !== null &&
      !target.closest('.card') &&
      !target.closest('.itinerary-dropdown')) {
      this.openItineraryMenuId = null;
    }
  }

  isLoggato(): boolean { return this.servAuth.isLoggato(); }
  ottieniRuolo(): string | null { return this.servAuth.ottieniRuolo(); }

  caricaItinerariUtente() {
    this.itinerarioService.getMieListe().subscribe({
      next: (liste) => {
        this.mieiItinerari = liste;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Ops, errore nel caricamento degli itinerari", err)
    });
  }

  avviaRicercaViaggi(evento?: Event) {
    if (evento) { evento.preventDefault(); }
    if (this.isLoading) return;

    this.haCercato = true;
    this.paginaCorrente = 0;
    this.eseguiRicerca();
  }

  eseguiRicerca() {
    if (this.isLoading) return;
    this.isLoading = true;

    const filtriPuliti: any = {};
    if (this.filtriViaggio.destinazione) filtriPuliti.destinazione = this.filtriViaggio.destinazione.trim();

    if (this.filtriViaggio.dataInizioMin) filtriPuliti.dataInizioMin = this.filtriViaggio.dataInizioMin + 'T00:00:00';
    if (this.filtriViaggio.dataFineMax) filtriPuliti.dataFineMax = this.filtriViaggio.dataFineMax + 'T23:59:59';

    if (this.filtriViaggio.maxPartecipanti) filtriPuliti.maxPartecipanti = Number(this.filtriViaggio.maxPartecipanti);

    if (this.filtriViaggio.prezzoMin) filtriPuliti.prezzoMin = Number(this.filtriViaggio.prezzoMin);
    if (this.filtriViaggio.prezzoMax) filtriPuliti.prezzoMax = Number(this.filtriViaggio.prezzoMax);

    this.zone.run(() => {
      this.viaggioService.getViaggi(this.paginaCorrente, filtriPuliti).subscribe({
        next: (risposta) => {
          this.viaggiTrovati = risposta.content ? [...risposta.content] : [];
          this.totalePagine = risposta.totalPages || 0;
          this.openItineraryMenuId = null;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Errore durante la ricerca viaggi', err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    });
  }

  paginaPrecedente() {
    if (this.paginaCorrente > 0 && !this.isLoading) {
      this.paginaCorrente--;
      this.eseguiRicerca();
      window.scrollTo({ top: 500, behavior: 'smooth' });
    }
  }

  paginaSuccessiva() {
    if (this.paginaCorrente < this.totalePagine - 1 && !this.isLoading) {
      this.paginaCorrente++;
      this.eseguiRicerca();
      window.scrollTo({ top: 500, behavior: 'smooth' });
    }
  }

  toggleItineraryMenu(viaggioId: number, event: Event) {
    event.stopPropagation();
    if (this.isLoading) return;

    if (this.openItineraryMenuId === viaggioId) {
      this.openItineraryMenuId = null;
    } else {
      this.openItineraryMenuId = viaggioId;
      this.nomeNuovoItinerario = '';
    }
  }

  selezionaItinerario(itinerarioId: number, viaggioId: number) {
    if (this.isLoading) return;
    this.isLoading = true;
    this.openItineraryMenuId = null;

    this.itinerarioService.aggiungiViaggioAItinerario(itinerarioId, viaggioId).subscribe({
      next: (risposta: any) => {
        alert(risposta.message || "Viaggio aggiunto con successo all'itinerario!");
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio", err);
        alert("Si è verificato un problema durante l'aggiunta del viaggio.");
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  apriModaleItinerario() {
    if (this.isLoading) return;
    this.nomeNuovoItinerario = '';
    this.modaleItinerarioAperta = true;
  }

  chiudiModaleItinerario() {
    if (this.isLoading) return;
    this.modaleItinerarioAperta = false;
  }

  salvaNuovoItinerario() {
    if (!this.nomeNuovoItinerario.trim() || this.isLoading) return;

    this.isLoading = true;
    this.cdr.detectChanges();

    this.itinerarioService.creaLista({
      nome: this.nomeNuovoItinerario.trim(),
      visibilita: 'PRIVATA'
    }).subscribe({
      next: (nuovaLista) => {
        this.mieiItinerari.push(nuovaLista);
        this.isLoading = false;
        this.chiudiModaleItinerario();

        alert("Itinerario creato con successo!");
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        alert("Errore durante la creazione. Riprova.");
        this.cdr.detectChanges();
      }
    });
  }

  // --- FUNZIONE PER IL CALCOLO DEI POSTI RIMANENTI ---
  calcolaPostiRimanenti(viaggio: any): number {
    if (!viaggio.maxPartecipanti) return 999;
    return viaggio.maxPartecipanti - (viaggio.partecipantiAttuali || 0);
  }

  // Viaggi che devono ancora iniziare
  get viaggiFuturi(): any[] {
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    return this.viaggiTrovati.filter(v => v.dataInizio && new Date(v.dataInizio) > oggi);
  }

  // Viaggi attualmente in corso
  get viaggiInCorso(): any[] {
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    return this.viaggiTrovati.filter(v => {
      if (!v.dataInizio || !v.dataFine) return false;
      const inizio = new Date(v.dataInizio);
      const fine = new Date(v.dataFine);
      return oggi >= inizio && oggi <= fine;
    });
  }

  // Viaggi storici terminati
  get viaggiTerminati(): any[] {
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    return this.viaggiTrovati.filter(v => v.dataFine && new Date(v.dataFine) < oggi);
  }

  isViaggioFuturo(viaggio: any): boolean {
    if (!viaggio || !viaggio.dataInizio) return false;
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    const dataPartenza = new Date(viaggio.dataInizio);
    return dataPartenza > oggi;
  }
}
