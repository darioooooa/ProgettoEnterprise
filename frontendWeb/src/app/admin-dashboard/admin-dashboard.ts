import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../service/admin.service';
import { SegnalazioneService } from '../service/segnalazione.service';
import { Router } from '@angular/router';

export interface RichiestaPromozione {
  id: number;
  usernameViaggiatore: string;
  emailViaggiatore: string;
  dataRichiesta: Date | string;
  motivazione: string;
  stato: string;
  biografiaProfessionale: string;
  documentiLink: string;
  adminId: number;
}

export interface Segnalazione {
  id: number;
  tipo: string;
  idRiferimento: number;
  motivo: string;
  descrizione: string;
  stato: string;
  dataCreazione: Date | string;
  segnalatoreId: number;
  segnalatoreUsername?: string;
  riferimentoNome?: string;
  adminId: number;
  adminUsername?: string;
}

export interface UtenteBannato {
  id: number;
  username: string;
  email: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  sezionePrincipale: 'PROMOZIONI' | 'SEGNALAZIONI' | 'UTENTI_BANNATI' = 'PROMOZIONI';
  adminUsername: string = 'Amministratore';
  adminIdLoggato: number = 0;

  richieste: RichiestaPromozione[] = [];
  vistaAttualePromozioni: 'PENDENTI' | 'STORICO' = 'PENDENTI';

  segnalazioni: Segnalazione[] = [];
  vistaAttualeSegnalazioni: 'DA_GESTIRE' | 'ARCHIVIO' = 'DA_GESTIRE';
  filtroTipo: string = '';
  caricamentoSegnalazioni: boolean = false;

  utentiBannati: UtenteBannato[] = [];

  mostraModaleMessaggio: boolean = false;
  messaggioInLettura: string = '';

  isLoading: boolean = false;

  constructor(
    private adminService: AdminService,
    private segnalazioneService: SegnalazioneService,
    private navigatore: Router,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.adminUsername = localStorage.getItem('username') || 'Amministratore';
      const idStr = localStorage.getItem('userId');
      this.adminIdLoggato = idStr ? Number(idStr) : 0;
    }

    this.caricaRichieste();
    this.caricaSegnalazioni();
    this.caricaUtentiBannati();
  }

  cambiaSezionePrincipale(sezione: 'PROMOZIONI' | 'SEGNALAZIONI' | 'UTENTI_BANNATI') {
    this.sezionePrincipale = sezione;
    if (sezione === 'UTENTI_BANNATI') {
      this.caricaUtentiBannati();
    }
  }

  cambiaVistaPromozioni(vista: 'PENDENTI' | 'STORICO') {
    this.vistaAttualePromozioni = vista;
  }

  get richiesteFiltrate() {
    if (this.vistaAttualePromozioni === 'PENDENTI') {
      return this.richieste.filter(r => r.stato === 'IN_ATTESA');
    } else {
      return this.richieste.filter(r => r.stato === 'APPROVATA' || r.stato === 'RIFIUTATA');
    }
  }

  caricaRichieste() {
    this.isLoading = true;
    this.adminService.getRichieste().subscribe({
      next: (dati) => {
        this.richieste = dati;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err)
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  approva(id: number) {
    if (!this.adminIdLoggato || this.isLoading) return;
    if (confirm("Approvare questa richiesta?")) {
      this.isLoading = true;
      this.adminService.approvaRichiesta(id, this.adminIdLoggato).subscribe({
        next: () => {
          alert('Approvata!');
          this.caricaRichieste();
        },
        error: () => {
          alert('Errore.')
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  rifiuta(id: number) {
    if (!this.adminIdLoggato || this.isLoading) return;
    const notaRifiuto = prompt("Inserisci motivazione:");
    if (notaRifiuto) {
      this.isLoading = true;
      this.adminService.rifiutaRichiesta(id, notaRifiuto, this.adminIdLoggato).subscribe({
        next: () => {
          alert('Rifiutata.');
          this.caricaRichieste();
        },
        error: () => {
          alert('Errore.')
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  cambiaVistaSegnalazioni(vista: 'DA_GESTIRE' | 'ARCHIVIO') {
    this.vistaAttualeSegnalazioni = vista;
  }

  get segnalazioniFiltrate() {
    let lista = this.segnalazioni;

    if (this.filtroTipo && this.filtroTipo !== '') {
      lista = lista.filter(s => s.tipo === this.filtroTipo);
    }

    if (this.vistaAttualeSegnalazioni === 'DA_GESTIRE') {
      return lista.filter(s => s.stato !== 'CHIUSA' && s.stato !== 'RIFIUTATA');
    } else {
      return lista.filter(s => s.stato === 'CHIUSA' || s.stato === 'RIFIUTATA');
    }
  }

  caricaSegnalazioni() {
    this.caricamentoSegnalazioni = true;
    this.isLoading = true;
    this.segnalazioneService.cercaSegnalazioni({}, 0).subscribe({
      next: (risposta) => {
        this.segnalazioni = risposta.content ? risposta.content : risposta;
        this.caricamentoSegnalazioni = false;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.caricamentoSegnalazioni = false;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  prendiInCarico(id: number) {
    if (!this.adminIdLoggato || this.isLoading) return;

    this.isLoading = true;
    this.segnalazioneService.prendiInCarico(id, this.adminIdLoggato).subscribe({
      next: () => {
        alert('Presa in carico!');
        this.caricaSegnalazioni();
      },
      error: (err) => {
        alert('Errore.')
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  risolviSegnalazione(id: number, sospendiAutore: boolean = false) {
    if (!this.adminIdLoggato || this.isLoading) return;

    const messaggioConferma = sospendiAutore
      ? "ATTENZIONE: Stai per applicare la sanzione più dura sull'elemento e sospendere definitivamente l'utente. Confermi?"
      : "Confermi la risoluzione della segnalazione (con eventuale rimozione dell'elemento)?";

    if (confirm(messaggioConferma)) {
      this.isLoading = true;
      this.segnalazioneService.risolviSegnalazione(id, this.adminIdLoggato, sospendiAutore).subscribe({
        next: () => {
          alert('Segnalazione risolta con successo!');
          this.caricaSegnalazioni();

          if (sospendiAutore) {
            this.caricaUtentiBannati();
          }
        },
        error: (err) => {
          alert('Errore durante la risoluzione.')
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  rifiutaSegnalazione(id: number) {
    if (!this.adminIdLoggato || this.isLoading) return;
    if (confirm("Rifiutare la segnalazione? L'elemento segnalato NON verrà eliminato.")) {
      this.isLoading = true;
      this.segnalazioneService.rifiutaSegnalazione(id, this.adminIdLoggato).subscribe({
        next: () => {
          alert('Segnalazione rifiutata.');
          this.caricaSegnalazioni();
        },
        error: (err) => {
          alert('Errore.')
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  caricaUtentiBannati() {
    this.isLoading = true;
    this.adminService.getUtentiBannati().subscribe({
      next: (dati) => {
        this.utentiBannati = dati;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err)
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  sbannaUtente(idUtente: number) {
    if (this.isLoading) return;
    if (confirm("Riattivare questo utente?")) {
      this.isLoading = true;
      this.adminService.sbannaUtente(idUtente).subscribe({
        next: () => {
          alert('Utente riattivato!');
          this.caricaUtentiBannati();
        },
        error: () => {
          alert('Errore.')
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  apriModaleMessaggio(testo?: string) {
    this.messaggioInLettura = testo || 'Testo non disponibile o contenuto già rimosso.';
    this.mostraModaleMessaggio = true;
  }

  chiudiModaleMessaggio() {
    this.mostraModaleMessaggio = false;
    this.messaggioInLettura = '';
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.clear();
    }
    this.navigatore.navigate(['/login']);
  }
}
