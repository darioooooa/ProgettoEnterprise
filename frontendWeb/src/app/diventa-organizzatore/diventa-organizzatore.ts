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
export class DiventaOrganizzatore implements OnInit {

  motivazione: string = '';
  biografiaProfessionale: string = '';
  documento: File | null = null;
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
        this.utenteId = parseInt(idStringa, 10);
      }
    }
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.documento = file;
    }
  }
  mostraModaleConferma: boolean = false;
  inviaRichiesta() {
    if (!this.motivazione || !this.biografiaProfessionale || !this.usernameRichiesto || !this.emailProfessionale) {
      this.messaggioErrore = 'Compila tutti i campi.';
      return;
    }
    this.mostraModaleConferma = true;
  }

  confermaRichiesta() {
    this.mostraModaleConferma = false;
    if (this.isLoading) return;
    this.isLoading = true;
    this.messaggioErrore = '';
    this.cdr.detectChanges();

    if (!this.motivazione || !this.biografiaProfessionale || !this.usernameRichiesto || !this.emailProfessionale) {
      this.messaggioErrore = 'Per favore, compila tutti i campi obbligatori prima di inviare.';
      return;
    }

    if (!this.documento) {
      this.messaggioErrore = 'È obbligatorio allegare un documento (CV o Portfolio).';
      return;
    }

    this.isLoading = true;

    const richiestaDto = {
      motivazione: this.motivazione,
      biografiaProfessionale: this.biografiaProfessionale,
      emailProfessionale: this.emailProfessionale,
      usernameRichiesto: this.usernameRichiesto,
      documentiLink: this.documento.name
    };

    const formData = new FormData();
    formData.append('richiesta', new Blob([JSON.stringify(richiestaDto)], { type: 'application/json' }));
    formData.append('file', this.documento);

    this.authService.inviaRichiestaPromozione(formData).subscribe({
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

        this.messaggioErrore = 'Si è verificato un errore imprevisto. Controlla i dati e riprova.';
        this.cdr.detectChanges();
      }
    });
  }

  chiudiModaleInSospeso() {
    this.mostraModaleInSospeso = false;
  }
}
