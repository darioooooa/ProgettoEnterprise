import { Component, OnInit, Input, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViaggioService } from '../../../service/viaggio.service';

@Component({
  selector: 'app-programma',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './programma.html',
  styleUrl: './programma.css'
})
export class ProgrammaComponent implements OnInit {
  @Input() viaggioId!: number;
  @Input() isMioViaggio: boolean = false;

  attivita: any[] = [];
  paginaAttivita = 0;
  totalePagineAttivita = 0;

  nuovaAttivita = { titolo: '', descrizione: '', orarioInizio: '', orarioFine: '', posizione: '', costo: 0 };
  idAttivitaDaEliminare: number | null = null;
  attivitaInModifica = false;
  idAttivitaInModifica: number | null = null;

  messaggioAvviso: string | null = null;
  tipoAvviso: 'successo' | 'errore' = 'errore';

  filtriAttivita = { titolo: '', posizione: '', costoMin: '', costoMax: '', orarioInizioMin: '', orarioInizioMax: '', orarioFineMin: '', orarioFineMax: '' };

  isLoading: boolean = false;

  constructor(private viaggioService: ViaggioService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.caricaAttivita();
  }

  caricaAttivita() {
    const filtriDaInviare = { ...this.filtriAttivita };

    if (this.filtriAttivita.orarioInizioMin && this.filtriAttivita.orarioInizioMin.trim() !== '') {
      filtriDaInviare.orarioInizioMin = this.filtriAttivita.orarioInizioMin + 'T00:00:00';
    } else { filtriDaInviare.orarioInizioMin = ''; }

    if (this.filtriAttivita.orarioInizioMax && this.filtriAttivita.orarioInizioMax.trim() !== '') {
      filtriDaInviare.orarioInizioMax = this.filtriAttivita.orarioInizioMax + 'T23:59:59';
    } else { filtriDaInviare.orarioInizioMax = ''; }

    if (this.filtriAttivita.orarioFineMin && this.filtriAttivita.orarioFineMin.trim() !== '') {
      filtriDaInviare.orarioFineMin = this.filtriAttivita.orarioFineMin + 'T00:00:00';
    } else { filtriDaInviare.orarioFineMin = ''; }

    if (this.filtriAttivita.orarioFineMax && this.filtriAttivita.orarioFineMax.trim() !== '') {
      filtriDaInviare.orarioFineMax = this.filtriAttivita.orarioFineMax + 'T23:59:59';
    } else { filtriDaInviare.orarioFineMax = ''; }

    this.viaggioService.getAttivitaViaggio(this.viaggioId, this.paginaAttivita, filtriDaInviare).subscribe({
      next: (res) => {
        this.attivita = res.content || [];
        this.totalePagineAttivita = res.totalPages || 0;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore nel caricamento delle attività filtrate:", err)
    });
  }

  filtraAttivita() {
    if (this.isLoading) return;
    this.paginaAttivita = 0;
    this.caricaAttivita();
  }

  pulisciFiltriAttivita() {
    if (this.isLoading) return;
    this.filtriAttivita = { titolo: '', posizione: '', costoMin: '', costoMax: '', orarioInizioMin: '', orarioInizioMax: '', orarioFineMin: '', orarioFineMax: '' };
    this.paginaAttivita = 0;
    this.caricaAttivita();
  }

  cambiaPaginaAttivita(dir: number) {
    if (this.isLoading) return;
    this.paginaAttivita += dir;
    this.caricaAttivita();
  }

  aggiungiTappaProgramma() {
    if (this.isLoading) return;

    if (!this.nuovaAttivita.titolo.trim()) {
      this.mostraErroreTappa("Il titolo della tappa è obbligatorio.");
      return;
    }
    if (!this.nuovaAttivita.orarioInizio || !this.nuovaAttivita.orarioFine) {
      this.mostraErroreTappa("Selezionare sia l'orario di inizio che l'orario di fine attività.");
      return;
    }
    if (!this.nuovaAttivita.posizione.trim() || this.nuovaAttivita.costo === null || this.nuovaAttivita.costo === undefined || isNaN(Number(this.nuovaAttivita.costo)) || Number(this.nuovaAttivita.costo) < 0) {
      this.mostraErroreTappa("Posizione e costo devono essere validi.");
      return;
    }
    this.messaggioAvviso = null;
    this.isLoading = true;

    const payload = {
      titolo: this.nuovaAttivita.titolo.trim(),
      descrizione: this.nuovaAttivita.descrizione ? this.nuovaAttivita.descrizione.trim() : '',
      orarioInizio: this.nuovaAttivita.orarioInizio,
      orarioFine: this.nuovaAttivita.orarioFine,
      posizione: this.nuovaAttivita.posizione.trim(),
      costo: Number(this.nuovaAttivita.costo)
    };

    if (this.attivitaInModifica && this.idAttivitaInModifica) {
      this.viaggioService.modificaAttivita(this.viaggioId, this.idAttivitaInModifica, payload).subscribe({
        next: () => {
          this.pulisciStatoFormAttivita();
          this.caricaAttivita();
          this.tipoAvviso = 'successo';
          this.messaggioAvviso = "Attività del programma aggiornata con successo!";
          this.isLoading = false;
          this.scattaScrollAvviso();
          setTimeout(() => this.messaggioAvviso === "Attività del programma aggiornata con successo!" && (this.messaggioAvviso = null), 4000);
        },
        error: (err) => {
          this.isLoading = false;
          this.gestisciErroreTappa(err);
        }
      });
    } else {
      this.viaggioService.creaAttivita(this.viaggioId, payload).subscribe({
        next: () => {
          this.pulisciStatoFormAttivita();
          this.caricaAttivita();
          this.tipoAvviso = 'successo';
          this.messaggioAvviso = "Nuova attività registrata nel programma!";
          this.isLoading = false;
          this.scattaScrollAvviso();
          setTimeout(() => this.messaggioAvviso === "Nuova attività registrata nel programma!" && (this.messaggioAvviso = null), 4000);
        },
        error: (err) => {
          this.isLoading = false;
          this.gestisciErroreTappa(err);
        }
      });
    }
  }

  private gestisciErroreTappa(err: any) {
    this.tipoAvviso = 'errore';
    this.messaggioAvviso = err.error?.messaggio || "Errore nel salvataggio dell'attività.";
    this.scattaScrollAvviso();
  }

  private mostraErroreTappa(msg: string) {
    this.tipoAvviso = 'errore';
    this.messaggioAvviso = msg;
    this.scattaScrollAvviso();
  }

  private scattaScrollAvviso() {
    this.cdr.detectChanges();
    setTimeout(() => {
      const elementoBanner = document.getElementById('ancora-avviso');
      if (elementoBanner) {
        elementoBanner.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        });
      }
    }, 50);
  }

  avviaModificaAttivita(att: any) {
    if (this.isLoading) return;
    this.attivitaInModifica = true;
    this.idAttivitaInModifica = att.id;

    this.nuovaAttivita = {
      titolo: att.titolo,
      descrizione: att.descrizione || '',
      orarioInizio: att.orarioInizio,
      orarioFine: att.orarioFine,
      posizione: att.posizione,
      costo: att.costo || 0
    };
    this.saltaASezione('sezione-programma');
  }

  saltaASezione(idSezione: string) {
    const elemento = document.getElementById(idSezione);
    if (elemento) {
      elemento.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
    }
  }

  annullaModificaAttivita() {
    if (this.isLoading) return;
    this.pulisciStatoFormAttivita();
  }

  private pulisciStatoFormAttivita() {
    this.nuovaAttivita = { titolo: '', descrizione: '', orarioInizio: '', orarioFine: '', posizione: '', costo: 0 };
    this.attivitaInModifica = false;
    this.idAttivitaInModifica = null;
    this.cdr.detectChanges();
  }

  cancellaTappaProgramma(idAttivita: number) {
    if (this.isLoading) return;

    if (this.idAttivitaDaEliminare !== idAttivita) {
      this.idAttivitaDaEliminare = idAttivita;
      setTimeout(() => this.idAttivitaDaEliminare === idAttivita && (this.idAttivitaDaEliminare = null), 4000);
      return;
    }
    this.idAttivitaDaEliminare = null;
    this.messaggioAvviso = null;
    this.isLoading = true;

    this.viaggioService.eliminaAttivita(this.viaggioId, idAttivita).subscribe({
      next: () => {
        if (this.idAttivitaInModifica === idAttivita) this.pulisciStatoFormAttivita();
        this.caricaAttivita();
        this.tipoAvviso = 'successo';
        this.messaggioAvviso = "Attività eliminata con successo dal programma.";
        this.isLoading = false;
        this.scattaScrollAvviso();
        setTimeout(() => this.messaggioAvviso === "Attività eliminata con successo dal programma." && (this.messaggioAvviso = null), 4000);
      },
      error: (err) => {
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = err.error?.messaggio || "Impossibile rimuovere la tappa selezionata.";
        this.isLoading = false;
        this.scattaScrollAvviso();
      }
    });
  }

  isStessoGiorno(d1: string, d2: string): boolean {
    if (!d1 || !d2) return true;
    return d1.split('T')[0] === d2.split('T')[0];
  }
}
