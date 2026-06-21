import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ViaggioService } from '../service/viaggio.service';
import { AutenticazioneService } from '../service/autenticazione.service';
import { GalleriaComponent } from './components/galleria/galleria';
import { ProgrammaComponent } from './components/programma/programma';
import { CommunityComponent } from './components/community/community';
import { PrenotazioneService } from '../service/prenotazione.service';
import { ModaleSegnalazione } from '../modale-segnalazione/modale-segnalazione';
import { forkJoin } from 'rxjs';
// 🟢 REINTRODOTTO L'IMPORT CHIRURGICO DELLA CHAT RECUPERATA
import { ChatComponent } from './components/chat/chat';

@Component({
  selector: 'app-dettaglio-viaggio',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    GalleriaComponent,
    ProgrammaComponent,
    CommunityComponent,
    RouterLink,
    ChatComponent, // 👈 Collegato correttamente ai metadati standalone
    ModaleSegnalazione
  ],
  templateUrl: './dettaglio-viaggio.html',
  styleUrl: './dettaglio-viaggio.css'
})
export class DettaglioViaggio implements OnInit {

  statistiche: any = {
    titolo: '',
    descrizione: '',
    dataInizio: '',
    dataFine: '',
    cittaPartenza: '',
    destinazione: '',
    prezzo: 0,
    partecipantiAttuali: 0,
    maxPartecipanti: 0,
    mediaRecensioni: 0,
    numeroRecensioni: 0,
    stato: 'APERTO',
    latitudine: 0,
    longitudine: 0
  };
  viaggioId!: number;
  organizzatoreUsername: string = '';
  mioUsername: string = '';


  tabAttivo: 'chat' | 'galleria' | 'programma' | 'community' = 'programma';

  isLoading: boolean = false;
  isEliminazioneInCorso: boolean = false;

  mostraSegnalazione = false;
  tipoDaSegnalare = '';
  idDaSegnalare = 0;

  // Variabili per la modifica in linea
  inModificaPrezzo: boolean = false;
  nuovoPrezzo: number = 0;

  isGiaAcquistato: boolean = false;
  statoSvolgimentoIscrizione: string = '';

  constructor(
    private route: ActivatedRoute,
    private viaggioService: ViaggioService,
    private servAuth: AutenticazioneService,
    private cdr: ChangeDetectorRef,
    private prenotazioneService: PrenotazioneService,
    private router: Router
  ) {}

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || '';
    this.viaggioId = Number(this.route.snapshot.paramMap.get('id'));

    if (this.isLoggato() && this.viaggioId) {
      this.caricaStatistichePadre();
    }
  }

  isLoggato(): boolean { return this.servAuth.isLoggato(); }
  ottieniRuolo(): string | null { return this.servAuth.ottieniRuolo(); }

  isMioViaggio(): boolean {
    if (!this.mioUsername || !this.organizzatoreUsername) return false;
    return this.isLoggato() &&
      this.ottieniRuolo() === 'ROLE_ORGANIZZATORE' &&
      this.mioUsername.trim().toLowerCase() === this.organizzatoreUsername.trim().toLowerCase();
  }

  caricaStatistichePadre() {
    this.isLoading = true;

    const chiamate: any = {
      datiViaggio: this.viaggioService.getViaggioById(this.viaggioId),
      stats: this.viaggioService.getStatisticheRecensioni(this.viaggioId)
    };
    if (this.isLoggato() && this.ottieniRuolo() === 'ROLE_VIAGGIATORE') {
      chiamate.infoPrenotazione = this.prenotazioneService.verificaPrenotazioneUtente(this.viaggioId);
    }

    // Chiamate parallele simultanee per la massima efficienza
    forkJoin(chiamate).subscribe({
      next: (risultati: any) => {
        setTimeout(() => {
          const { datiViaggio, stats, infoPrenotazione } = risultati;

          this.statistiche = {
            ...this.statistiche,
            ...stats,
            ...datiViaggio
          };

          this.statistiche.prezzo = datiViaggio?.prezzo ?? 0;
          this.statistiche.partecipantiAttuali = datiViaggio?.partecipantiAttuali ?? 0;
          this.statistiche.maxPartecipanti = datiViaggio?.maxPartecipanti ?? 0;

          this.statistiche.latitudine = datiViaggio?.latitudine ?? 0;
          this.statistiche.longitudine = datiViaggio?.longitudine ?? 0;

          this.statistiche.mediaRecensioni = stats?.mediaRecensioni ?? 0;
          this.statistiche.numeroRecensioni = stats?.numeroRecensioni ?? 0;

          this.statistiche.stato = datiViaggio?.stato || 'APERTO';

          this.organizzatoreUsername = stats?.organizzatoreUsername || datiViaggio?.organizzatoreUsername || '';

          // Blocca l'utente e assegna lo stato temporale solo se "acquistata" è true (stato CONFERMATA)
          if (infoPrenotazione && infoPrenotazione.acquistata) {
            this.isGiaAcquistato = true;
            this.statoSvolgimentoIscrizione = this.calcolaSvolgimentoReale(datiViaggio);
          } else {
            this.isGiaAcquistato = false;
            this.statoSvolgimentoIscrizione = '';
          }

          this.isLoading = false;
          this.cdr.markForCheck();
          this.cdr.detectChanges();
        }, 50);

      },
      error: (err) => {
        console.error("Errore critico durante il caricamento parallelo:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Calcola lo svolgimento temporale reale basandosi sulle date
  private calcolaSvolgimentoReale(viaggio: any): string {
    if (!viaggio?.dataInizio || !viaggio?.dataFine) return 'PRENOTATO';
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    const inizio = new Date(viaggio.dataInizio);
    const fine = new Date(viaggio.dataFine);

    if (oggi < inizio) return 'PRENOTATO';
    if (oggi >= inizio && oggi <= fine) return 'IN_CORSO';
    return 'COMPLETATO';
  }

  // Invocato tramite @Output dai figli se un'azione richiede di aggiornare i voti in cima
  onSincronizzaRichiesta() {
    this.caricaStatistichePadre();
  }

  scaricaFileIcs() {
    if (!this.viaggioId || this.isLoading) return;

    this.isLoading = true;

    this.prenotazioneService.scaricaFileIcs(this.viaggioId).subscribe({
      next: (fileBlob: Blob) => {
        const blobUrl = window.URL.createObjectURL(fileBlob);
        const linkDownload = document.createElement('a');
        linkDownload.href = blobUrl;
        linkDownload.download = `prenotazione_${this.viaggioId}.ics`;

        linkDownload.click();
        window.URL.revokeObjectURL(blobUrl);

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il download del calendario .ics:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  apriSegnalazioneOrg(idOrg: number, event: Event) {
    event.stopPropagation();
    if (this.isLoading) return;
    this.tipoDaSegnalare = 'UTENTE';
    this.idDaSegnalare = idOrg;
    this.mostraSegnalazione = true;
  }

  eliminaViaggio(idViaggio: number) {
    const conferma = confirm('ATTENZIONE: Sei sicuro di voler cancellare questo viaggio? Tutti i partecipanti verranno automaticamente rimborsati tramite Stripe.');

    if (conferma) {
      this.isEliminazioneInCorso = true;

      this.viaggioService.eliminaViaggio(idViaggio).subscribe({
        next: (response) => {
          this.isEliminazioneInCorso = false;
          alert('Viaggio cancellato e rimborsi inviati con successo!');
          this.router.navigate(['/organizzatore']);
        },
        error: (errore) => {
          this.isEliminazioneInCorso = false;
          console.error("Errore durante l'eliminazione:", errore);
          alert('Si è verificato un errore durante la cancellazione del viaggio.');
        }
      });
    }
  }

  // --- METODI PER LA MODIFICA IN LINEA ---
  attivaModificaPrezzo() {
    this.nuovoPrezzo = this.statistiche.prezzo;
    this.inModificaPrezzo = true;
  }

  annullaModificaPrezzo() {
    this.inModificaPrezzo = false;
  }

  salvaPrezzo() {
    if (this.isLoading) return;
    this.isLoading = true;

    const viaggioAggiornato = {
      titolo: this.statistiche.titolo,
      descrizione: this.statistiche.descrizione,
      destinazione: this.statistiche.destinazione,
      cittaPartenza: this.statistiche.cittaPartenza,
      dataInizio: this.statistiche.dataInizio,
      dataFine: this.statistiche.dataFine,
      prezzo: this.nuovoPrezzo,
      maxPartecipanti: this.statistiche.maxPartecipanti,
      latitudine: this.statistiche.latitudine,
      longitudine: this.statistiche.longitudine
    };

    this.viaggioService.modificaViaggio(this.viaggioId, viaggioAggiornato).subscribe({
      next: (risposta) => {
        this.statistiche.prezzo = this.nuovoPrezzo;
        this.inModificaPrezzo = false;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante l'aggiornamento del prezzo:", err);
        alert("Impossibile aggiornare il prezzo in questo momento.");
        this.inModificaPrezzo = false;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get isTuttoEsaurito(): boolean {
    if (!this.statistiche) return false;
    const attuali = this.statistiche.partecipantiAttuali ?? 0;
    const max = this.statistiche.maxPartecipanti ?? 0;
    return max > 0 && attuali >= max;
  }
}
