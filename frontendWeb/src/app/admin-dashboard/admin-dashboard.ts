import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core'; // Aggiunti Inject e PLATFORM_ID
import { CommonModule, isPlatformBrowser } from '@angular/common';      // Aggiunto isPlatformBrowser
import { AdminService } from '../service/admin.service';
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

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboardComponent implements OnInit {

  richieste: RichiestaPromozione[] = [];
  adminUsername: string = 'Amministratore';
  vistaAttuale: 'PENDENTI' | 'STORICO' = 'PENDENTI';

  constructor(
    private adminService: AdminService,
    private navigatore: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.caricaRichieste();

    if (isPlatformBrowser(this.platformId)) {
      this.adminUsername = localStorage.getItem('username') || 'Amministratore';
    }
  }

  cambiaVista(vista: 'PENDENTI' | 'STORICO') {
    this.vistaAttuale = vista;
  }

  get richiesteFiltrate() {
    if (this.vistaAttuale === 'PENDENTI') {
      return this.richieste.filter(r => r.stato === 'IN_ATTESA');
    } else {
      return this.richieste.filter(r => r.stato === 'APPROVATA' || r.stato === 'RIFIUTATA');
    }
  }

  caricaRichieste() {
    this.adminService.getRichieste().subscribe({
      next: (datiDalService) => {
        setTimeout(() => {
          this.richieste = datiDalService;
        });
      },
      error: (errore) => {
        console.error('Errore durante il recupero delle richieste: ', errore);
      }
    });
  }

  approva(id: number) {
    if (isPlatformBrowser(this.platformId)) {
      const adminIdString = localStorage.getItem('adminId');

      if (!adminIdString) {
        alert('Errore: ID amministratore non trovato. Effettua nuovamente il login.');
        return;
      }

      const adminId = Number(adminIdString);

      if (confirm("Sei sicuro di voler approvare questa richiesta?")) {
        this.adminService.approvaRichiesta(id, adminId).subscribe({
          next: (risposta) => {
            alert('Richiesta approvata con successo!');
            this.caricaRichieste();
          },
          error: (err) => {
            console.error("Errore durante l'approvazione", err);
            alert('Errore durante l\'approvazione.');
          }
        });
      }
    }
  }

  rifiuta(id: number) {
    if (isPlatformBrowser(this.platformId)) {
      const adminIdString = localStorage.getItem('adminId');

      if (!adminIdString) {
        alert('Errore: ID amministratore non trovato. Effettua nuovamente il login.');
        return;
      }

      const adminId = Number(adminIdString);
      const notaRifiuto = prompt("Inserisci la motivazione del rifiuto:");

      if (notaRifiuto) {
        this.adminService.rifiutaRichiesta(id, notaRifiuto, adminId).subscribe({
          next: (risposta) => {
            alert('Richiesta rifiutata.');
            this.caricaRichieste();
          },
          error: (err) => {
            console.error("Errore durante il rifiuto", err);
            alert('Errore durante il rifiuto della richiesta.');
          }
        });
      }
    }
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.clear();
    }
    this.navigatore.navigate(['/login']);
  }
}
