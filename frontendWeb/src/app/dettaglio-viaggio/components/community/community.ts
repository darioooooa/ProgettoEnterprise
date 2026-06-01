import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViaggioService } from '../../../service/viaggio.service';
import { AutenticazioneService } from '../../../service/autenticazione.service';

@Component({
  selector: 'app-community',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './community.html',
  styleUrl: './community.css'
})
export class CommunityComponent implements OnInit {
  @Input() viaggioId!: number;
  @Input() mioUsername!: string;
  @Input() isMioViaggio: boolean = false;

  // Notifica il padre di aggiornare la media e i conteggi totali voti
  @Output() richiestaRefreshPadre = new EventEmitter<void>();

  recensioni: any[] = [];
  paginaRecensioni = 0;
  totalePagineRecensioni = 0;

  nuovaRecensione = { voto: 5, commento: '' };
  inModifica = false;
  idRecensioneInModifica: number | null = null;
  laMiaRecensione: any = null;
  copiaLaMiaRecensione: any = null;
  idRecensioneDaEliminare: number | null = null;

  messaggioAvviso: string | null = null;
  tipoAvviso: 'successo' | 'errore' = 'errore';

  filtriRecensioni = { votoEsatto: '', votoMin: '', votoMax: '', parolaChiave: '', dataDa: '', dataA: '' };

  constructor(
    private viaggioService: ViaggioService,
    private auth: AutenticazioneService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.caricaRecensioni();
  }

  isLoggato(): boolean { return this.auth.isLoggato(); }
  ottieniRuolo(): string | null { return this.auth.ottieniRuolo(); }

  caricaRecensioni() {
    const filtriDaInviare = {...this.filtriRecensioni};
    if (this.filtriRecensioni.votoEsatto) {
      filtriDaInviare.votoMin = this.filtriRecensioni.votoEsatto;
      filtriDaInviare.votoMax = this.filtriRecensioni.votoEsatto;
    } else {
      filtriDaInviare.votoMin = '';
      filtriDaInviare.votoMax = '';
    }

    if (this.filtriRecensioni.dataDa) filtriDaInviare.dataDa = this.filtriRecensioni.dataDa + 'T00:00:00';
    if (this.filtriRecensioni.dataA) filtriDaInviare.dataA = this.filtriRecensioni.dataA + 'T23:59:59';

    this.viaggioService.getRecensioni(this.viaggioId, this.paginaRecensioni, filtriDaInviare).subscribe({
      next: (res) => {
        this.recensioni = res.content || [];
        this.totalePagineRecensioni = res.totalPages || 0;
        if (this.isLoggato() && this.ottieniRuolo() === 'ROLE_VIAGGIATORE') {
          const match = this.recensioni.find(r => r.utenteUsername === this.mioUsername);
          if (match) {
            this.laMiaRecensione = match;
            this.copiaLaMiaRecensione = { ...match };
          }
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore durante il caricamento delle recensioni filtrate:", err)
    });
  }

  filtraRecensioni() {
    this.paginaRecensioni = 0;
    this.caricaRecensioni();
  }

  pulisciFiltriRecensioni() {
    this.filtriRecensioni = { votoEsatto: '', votoMin: '', votoMax: '', parolaChiave: '', dataDa: '', dataA: '' };
    this.paginaRecensioni = 0;
    this.caricaRecensioni();
  }

  cambiaPaginaRecensioni(dir: number) {
    this.paginaRecensioni += dir;
    this.caricaRecensioni();
  }

  aggiungiRecensione() {
    if (!this.nuovaRecensione.commento.trim()) this.nuovaRecensione.commento = '';
    this.messaggioAvviso = null;

    if (this.inModifica && this.idRecensioneInModifica) {
      this.viaggioService.aggiornaRecensione(this.viaggioId, this.idRecensioneInModifica, this.nuovaRecensione).subscribe({
        next: () => {
          this.pulisciStatoFormRecensione();
          setTimeout(() => {
            this.caricaRecensioni();
            this.richiestaRefreshPadre.emit();
            this.tipoAvviso = 'successo';
            this.messaggioAvviso = "Recensione aggiornata con successo!";
            this.cdr.detectChanges();
            setTimeout(() => this.messaggioAvviso === "Recensione aggiornata con successo!" && (this.messaggioAvviso = null), 4000);
          }, 300);
        },
        error: (err) => this.gestisciErroreRecensione(err)
      });
    } else {
      this.viaggioService.inviaRecensione(this.viaggioId, this.nuovaRecensione).subscribe({
        next: () => {
          this.pulisciStatoFormRecensione();
          setTimeout(() => {
            this.caricaRecensioni();
            this.richiestaRefreshPadre.emit();
            this.tipoAvviso = 'successo';
            this.messaggioAvviso = "Recensione pubblicata con successo!";
            this.scattaScrollAvviso();
            setTimeout(() => {
              if (this.messaggioAvviso === "Recensione pubblicata con successo!") {
                this.messaggioAvviso = null;
                this.cdr.detectChanges();
              }
            }, 4000);
          }, 300);
        },
        error: (err) => {
          this.gestisciErroreRecensione(err)
        }
      });
    }
  }

  private gestisciErroreRecensione(err: any) {
    console.error("Errore recensione:", err);
    this.tipoAvviso = 'errore';
    this.messaggioAvviso = err.error?.messaggio || err.error?.message || "Si è verificato un errore imprevisto.";
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


  avviaModificaRecensione(rec: any) {
    this.inModifica = true;
    this.idRecensioneInModifica = rec.id;
    this.copiaLaMiaRecensione = { ...rec };
    this.nuovaRecensione = { voto: rec.voto, commento: rec.commento || '' };
    this.saltaASezione('sezione-community');
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

  annullaModificaRecensione() {
    this.nuovaRecensione = { voto: 5, commento: '' };
    this.inModifica = false;
    this.idRecensioneInModifica = null;

    this.laMiaRecensione = { ...this.copiaLaMiaRecensione };

    this.cdr.detectChanges();
  }

  private pulisciStatoFormRecensione() {
    this.nuovaRecensione = { voto: 5, commento: '' };
    this.inModifica = false;
    this.idRecensioneInModifica = null;
    this.laMiaRecensione = null;
    this.copiaLaMiaRecensione = null;
    this.paginaRecensioni = 0;
    this.cdr.detectChanges();
  }


  cancellaRecensione(idRecensione: number) {
    if (this.idRecensioneDaEliminare !== idRecensione) {
      // Primo click
      this.idRecensioneDaEliminare = idRecensione;

      // Se l'utente non clicca di nuovo entro 4 secondi, resetta lo stato
      setTimeout(() => {
        if (this.idRecensioneDaEliminare === idRecensione) {
          this.idRecensioneDaEliminare = null;
          this.cdr.detectChanges();
        }
      }, 4000);

      return; // Interrompe il metodo in attesa del secondo click
    }

    // Secondo click: esegue l'eliminazione reale sul database
    this.idRecensioneDaEliminare = null;
    this.messaggioAvviso = null;

    this.viaggioService.eliminaRecensione(this.viaggioId, idRecensione).subscribe({
      next: () => {
        this.pulisciStatoFormRecensione();

        setTimeout(() => {
          this.caricaRecensioni();

          this.richiestaRefreshPadre.emit();

          // Banner di successo temporaneo
          this.tipoAvviso = 'successo';
          this.messaggioAvviso = "Recensione eliminata.";
          this.scattaScrollAvviso();

          setTimeout(() => {
            if (this.messaggioAvviso === "Recensione eliminata.") {
              this.messaggioAvviso = null;
              this.cdr.detectChanges();
            }
          }, 4000);
        }, 300);
      },
      error: (err) => {
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = err.error?.messaggio || err.error?.message || "Errore nell'eliminazione della recensione. Non hai i permessi.";
        this.scattaScrollAvviso();
      }
    });
  }

}
