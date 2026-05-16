import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; // Aggiunto ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import { AmiciziaService } from '../service/amicizia.service';

@Component({
  selector: 'app-schermata-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schermataHome.html',
  styleUrl: './schermataHome.css'
})
export class SchermataHomeComponent implements OnInit {
  mostraMenu = false;
  titolo = 'Benvenuti in TravelBooking';

  viaggiEsempio = [
    { destinazione: 'Roma', descrizione: 'Tour del Colosseo', prezzo: 45 },
    { destinazione: 'Parigi', descrizione: 'Crociera sulla Senna', prezzo: 60 }
  ];

  // --- DATI MODALE AMICI, RICHIESTE E RICERCA ---
  modaleAmiciAperta = false;
  vistaAttuale: 'listaAmici' | 'itinerariAmico' = 'listaAmici';
  schedaAttiva: 'amici' | 'richieste' | 'cerca' = 'amici';

  listaAmici: any[] = [];
  richiesteRicevute: any[] = [];
  amicoSelezionato: any = null;
  itinerariAmico: any[] = [];
  mioUsername: string = '';

  // Variabili dedicate alla ricerca utenti
  usernameCercato: string = '';
  utenteTrovato: any = null;
  erroreRicerca: string = '';
  richiestaInviataConSuccesso = false;

  constructor(
    private servAuth: AutenticazioneService,
    private amiciziaService: AmiciziaService,
    private cdr: ChangeDetectorRef // Iniettato qui per svegliare la grafica
  ) {
    console.log('Schermata Home inizializzata!');
  }

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || 'Utente';
    if (this.isLoggato()) {
      this.caricaDatiAmicizieInBackground();
    }
  }

  isLoggato(): boolean {
    return this.servAuth.isLoggato();
  }

  toggleMenu() {
    this.mostraMenu = !this.mostraMenu;
  }

  logout() {
    this.servAuth.esci();
  }

  ottieniUsername(): string | null {
    return this.servAuth.ottieniUsername();
  }

  caricaDatiAmicizieInBackground() {
    this.amiciziaService.ottieniMieiAmici().subscribe({
      next: (amici) => {
        this.listaAmici = amici;
        this.cdr.detectChanges(); // Forza il refresh
      },
      error: (err) => console.error("Errore recupero amici", err)
    });

    this.amiciziaService.ottieniRichiesteRicevute().subscribe({
      next: (richieste) => {
        this.richiesteRicevute = richieste;
        this.cdr.detectChanges(); // Forza il refresh
      },
      error: (err) => console.error("Errore recupero richieste", err)
    });
  }

  apriModaleAmici() {
    this.mostraMenu = false;
    this.vistaAttuale = 'listaAmici';
    this.schedaAttiva = 'amici';
    this.pulisciRicerca();
    this.modaleAmiciAperta = true;
    this.caricaDatiAmicizieInBackground();
  }

  chiudiModale() {
    this.modaleAmiciAperta = false;
    this.amicoSelezionato = null;
  }

  cambiaScheda(scheda: 'amici' | 'richieste' | 'cerca') {
    this.schedaAttiva = scheda;
    if (scheda === 'cerca') {
      this.pulisciRicerca();
    }
    this.cdr.detectChanges();
  }

  pulisciRicerca() {
    this.usernameCercato = '';
    this.utenteTrovato = null;
    this.erroreRicerca = '';
    this.richiestaInviataConSuccesso = false;
    this.cdr.detectChanges();
  }

  cercaUtente() {
    this.erroreRicerca = '';
    this.utenteTrovato = null;
    this.richiestaInviataConSuccesso = false;

    if (!this.usernameCercato.trim()) return;

    if (this.usernameCercato.toLowerCase() === this.mioUsername.toLowerCase()) {
      this.erroreRicerca = "Non puoi cercare te stesso!";
      return;
    }

    this.servAuth.ottieniDatiUtenteDalDatabase(this.usernameCercato.trim()).subscribe({
      next: (utente) => {
        this.utenteTrovato = utente;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erroreRicerca = "Utente non trovato.";
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  inviaRichiestaAmicizia(riceventeUsername: string) {
    this.amiciziaService.inviaRichiesta(riceventeUsername).subscribe({
      next: () => {
        this.richiestaInviataConSuccesso = true;
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
      },
      error: (err) => {
        this.erroreRicerca = "Esiste già una richiesta pendente o un'amicizia attiva con questo utente.";
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  accettaRichiesta(amiciziaId: number) {
    this.amiciziaService.accettaRichiesta(amiciziaId).subscribe({
      next: (amiciziaAggiornata) => {
        const richiestaAccettata = this.richiesteRicevute.find(r => r.id === amiciziaId);

        if (richiestaAccettata) {
          this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
          amiciziaAggiornata.stato = 'ACCETTATA';
          this.listaAmici = [...this.listaAmici, amiciziaAggiornata];
        }
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
      },
      error: (err) => console.error("Errore nell'accettare l'amicizia", err)
    });
  }

  rifiutaRichiesta(amiciziaId: number) {
    this.amiciziaService.rifiutaRichiesta(amiciziaId).subscribe({
      next: () => {
        this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
      },
      error: (err) => console.error("Errore nel rifiutare l'amicizia", err)
    });
  }

  vediItinerari(amico: any) {
    this.amicoSelezionato = amico;
    this.vistaAttuale = 'itinerariAmico';
    this.itinerariAmico = [];
    this.cdr.detectChanges();
  }

  tornaIndietro() {
    this.vistaAttuale = 'listaAmici';
    this.amicoSelezionato = null;
    this.itinerariAmico = [];
    this.cdr.detectChanges();
  }
}
