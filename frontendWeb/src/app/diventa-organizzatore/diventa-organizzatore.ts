import { Component, Inject, OnInit, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';

@Component({
  selector: 'app-diventa-organizzatore',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './diventa-organizzatore.html',
  styleUrl: './diventa-organizzatore.css'
})
export class DiventaOrganizzatoreComponent implements OnInit {

  motivazione: string = '';
  biografiaProfessionale: string = '';
  documentiLink: string = '';
  usernameRichiesto: string = '';
  emailProfessionale: string = '';
  messaggioErrore: string = '';

  utenteId: number | null = null;

  isLoading: boolean = false;
  mostraModaleInSospeso: boolean = false;

  constructor(
    private authService: AutenticazioneService,
    private navigatore: Router,
    @Inject(PLATFORM_ID) private platformId: Object,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      const idStringa = localStorage.getItem('userId');
      if (idStringa) {
        this.utenteId = Number(idStringa);
      }
    }
  }

  chiudiModaleInSospeso() {
    this.mostraModaleInSospeso = false;
  }

  inviaRichiesta() {
    if (this.isLoading) return;

    this.messaggioErrore = '';
    this.cdr.detectChanges();

    if (!this.motivazione || !this.biografiaProfessionale || !this.documentiLink ||
      !this.usernameRichiesto || !this.emailProfessionale) {
      this.messaggioErrore = 'Per favore, compila tutti i campi prima di inviare.';
      return;
    }

    if (isPlatformBrowser(this.platformId)) {
      const idStringa = localStorage.getItem('userId');
      if (idStringa) {
        this.utenteId = Number(idStringa);
      }
    }

    if (!this.utenteId) {
      this.messaggioErrore = 'Errore: ID utente non trovato in memoria. Effettua nuovamente il login.';
      return;
    }

    this.isLoading = true;

    const payload = {
      motivazione: this.motivazione,
      biografiaProfessionale: this.biografiaProfessionale,
      documentiLink: this.documentiLink,
      emailProfessionale: this.emailProfessionale,
      usernameRichiesto: this.usernameRichiesto,
    };

    this.authService.inviaRichiestaPromozione(this.utenteId, payload).subscribe({
      next: (risposta) => {
        this.isLoading = false;
        this.navigatore.navigate(['/home']);
      },
      error: (errore) => {
        console.error('ERRORE RICEVUTO:', errore);
        this.isLoading = false;

        if (errore.status === 400 || errore.status === 409) {
          this.mostraModaleInSospeso = true;
          this.cdr.detectChanges();
          return;
        }

        if (errore.status === 401 || errore.status === 403) {
          this.messaggioErrore = 'Errore di autenticazione. Effettua nuovamente il login.';
          this.cdr.detectChanges();
          return;
        }

        let estratto = 'Si è verificato un errore imprevisto. Controlla i dati e riprova.';
        let erroreCorpo = errore.error;

        console.log('CORPO ERRORE DAL BACKEND:', erroreCorpo);

        if (erroreCorpo) {
          if (typeof erroreCorpo === 'string') {
            try {
              const parsed = JSON.parse(erroreCorpo);
              estratto = parsed.messaggio || parsed.message || erroreCorpo;
            } catch (e) {
              estratto = erroreCorpo;
            }
          } else {
            estratto = erroreCorpo.messaggio || erroreCorpo.message || estratto;
          }
        }

        console.log("MESSAGGIO CHE APPARIRÀ NEL BOX ROSSO:", estratto);

        setTimeout(() => {
          this.messaggioErrore = estratto;
          this.cdr.detectChanges();
        });
      }
    });
  }
}
