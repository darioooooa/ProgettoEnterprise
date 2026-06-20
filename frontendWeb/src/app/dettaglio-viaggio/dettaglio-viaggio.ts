import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import { ViaggioService } from '../service/viaggio.service';
import { AutenticazioneService } from '../service/autenticazione.service';
import { GalleriaComponent } from './components/galleria/galleria';
import { ProgrammaComponent } from './components/programma/programma';
import { CommunityComponent } from './components/community/community';
import {PrenotazioneService} from '../service/prenotazione.service';
import { ModaleSegnalazione } from '../modale-segnalazione/modale-segnalazione';
import { forkJoin } from 'rxjs';


@Component({
  selector: 'app-dettaglio-viaggio',
  standalone: true,
  imports: [
    CommonModule,
    GalleriaComponent,
    ProgrammaComponent,
    CommunityComponent,
    RouterLink,

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
    numeroRecensioni: 0
  };
  viaggioId!: number;
  organizzatoreUsername: string = '';
  mioUsername: string = '';

  // Stato del tab attivo
  tabAttivo: 'galleria' | 'programma' | 'community' = 'programma'  ;

  isLoading: boolean = false;

  isEliminazioneInCorso: boolean = false;

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

    // Chiamate parallele simultanee per la massima efficienza
    forkJoin({
      datiViaggio: this.viaggioService.getViaggioById(this.viaggioId),
      stats: this.viaggioService.getStatisticheRecensioni(this.viaggioId)
    }).subscribe({
      next: ({ datiViaggio, stats }: { datiViaggio: any, stats: any }) => {

        setTimeout(() => {
          // Unione
          this.statistiche = {
            ...this.statistiche,
            ...stats,
            ...datiViaggio
          };

          this.statistiche.prezzo = datiViaggio?.prezzo ?? 0;
          this.statistiche.partecipantiAttuali = datiViaggio?.partecipantiAttuali ?? 0;
          this.statistiche.maxPartecipanti = datiViaggio?.maxPartecipanti ?? 0;

          // Recupero metriche recensioni
          this.statistiche.mediaRecensioni = stats?.mediaRecensioni ?? 0;
          this.statistiche.numeroRecensioni = stats?.numeroRecensioni ?? 0;

          this.organizzatoreUsername = stats?.organizzatoreUsername || datiViaggio?.organizzatoreUsername || '';
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

  // Invocato tramite @Output dai figli se un'azione richiede di aggiornare i voti in cima
  onSincronizzaRichiesta() {
    this.caricaStatistichePadre();
  }
  scaricaFileIcs() {
    if (!this.viaggioId || this.isLoading) return;

    this.isLoading = true;

    this.prenotazioneService.scaricaFileIcs(this.viaggioId).subscribe({
      next: (fileBlob: Blob) => {
        // Creiamo l'URL di memoria nel browser
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
  mostraSegnalazione = false;
  tipoDaSegnalare = '';
  idDaSegnalare = 0;

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
}
