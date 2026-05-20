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
  schedaAttiva: 'amici' | 'richieste' | 'inviate' | 'cerca' = 'amici';

  listaAmici: any[] = [];
  richiesteRicevute: any[] = [];
  richiesteInviate: any[] = [];
  richiesteRifiutate: any[] = [];
  amicoSelezionato: any = null;
  itinerariAmico: any[] = [];
  mioUsername: string = '';

  // Variabili dedicate alla ricerca utenti
  usernameCercato: string = '';
  utentiTrovati: any[] = [];
  erroreRicerca: string = '';
  richiesteCompletate: string[] = [];

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

    // Amici accettati
    this.amiciziaService.ottieniMieiAmici().subscribe({
      next: (amici) => {
        this.listaAmici = amici;
        this.cdr.detectChanges(); // Forza il refresh
      },
      error: (err) => console.error("Errore recupero amici", err)
    });

    // Richieste in arrivo
    this.amiciziaService.ottieniRichiesteRicevute().subscribe({
      next: (richieste) => {
        this.richiesteRicevute = richieste;
        this.cdr.detectChanges(); // Forza il refresh
      },
      error: (err) => console.error("Errore recupero richieste", err)
    });

    // Richieste inviate
    this.amiciziaService.ottieniRichiesteInviate().subscribe({
      next: (inviate) => {
        this.richiesteInviate = inviate;
        this.richiesteCompletate = inviate.map(r => r.riceventeUsername);
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore recupero richieste inviate", err)
    });

    // Recupera le richieste rifiutate
    this.amiciziaService.ottieniRichiesteRifiutate().subscribe({
      next: (rifiutate) => {
        this.richiesteRifiutate = rifiutate;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore recupero richieste rifiutate", err)
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

  cambiaScheda(scheda: 'amici' | 'richieste' | 'inviate' | 'cerca') {
    this.schedaAttiva = scheda;
    if (scheda === 'cerca') {
      this.pulisciRicerca();
    }
    this.caricaDatiAmicizieInBackground();
    this.cdr.detectChanges();
  }

  pulisciRicerca() {
    this.usernameCercato = '';
    this.utentiTrovati = [];
    this.erroreRicerca = '';
    this.cdr.detectChanges();
  }

  cercaUtente() {
    this.erroreRicerca = '';
    this.utentiTrovati = [];

    if (!this.usernameCercato.trim()) return;

    if (this.usernameCercato.toLowerCase() === this.mioUsername.toLowerCase()) {
      this.erroreRicerca = "Non puoi cercare te stesso!";
      return;
    }

    this.servAuth.ottieniDatiUtenteDalDatabase(this.usernameCercato.trim()).subscribe({
      next: (rispostaArray) => {
        if (rispostaArray && rispostaArray.length > 0) {
          this.utentiTrovati = rispostaArray.filter((utente: any) =>
            utente.username.toLowerCase() !== this.mioUsername.toLowerCase()
          );
          if (this.utentiTrovati.length === 0) {
            this.erroreRicerca = "Nessun altro utente trovato con questo nome.";
          }
        } else {
          this.erroreRicerca = "Nessun utente trovato con questo nome.";
        }
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
        this.richiesteCompletate = [...this.richiesteCompletate, riceventeUsername];
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

  // Metodo per sapere se mostrare il bottone o la spunta
  haInviatoRichiesta(username: string): boolean {
    return this.richiesteCompletate.includes(username);
  }
  haRicevutoRichiesta(username: string): boolean {
    return this.richiesteRicevute.some(req => req.richiedenteUsername === username);
  }

  isRichiestaRifiutata(usernameCercato: string): boolean {
    return this.richiesteRifiutate.some(req =>
      req.richiedenteUsername === usernameCercato ||
      req.riceventeUsername === usernameCercato
    );
  }

  // Metodo peer controllare se l'utente cercato è già nella lista amici
  sonoGiaAmici(usernameCercato: string): boolean {
    return this.listaAmici.some(amico =>
      amico.richiedenteUsername === usernameCercato ||
      amico.riceventeUsername === usernameCercato
    );
  }

  accettaRichiesta(amiciziaId: number) {
    this.amiciziaService.accettaRichiesta(amiciziaId).subscribe({
      next: (amiciziaAggiornata) => {
        this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
      },
      error: (err) => console.error("Errore nell'accettare l'amicizia", err)
    });
  }

  rifiutaRichiesta(amiciziaId: number) {
    this.amiciziaService.rifiutaRichiesta(amiciziaId).subscribe({
      next: () => {
        const richiestaDaRifiutare = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
        if (richiestaDaRifiutare) {
          this.richiesteRifiutate.push(richiestaDaRifiutare);
        }
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
