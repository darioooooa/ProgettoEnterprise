import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AutenticazioneService } from '../../service/autenticazione.service';
import { AmiciziaService } from '../../service/amicizia.service';
import { ChatService } from '../../service/chat.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, FormsModule],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css'
})
export class MainLayoutComponent implements OnInit {
  mostraMenu: boolean = false;
  mioUsername: string = '';
  notificheTotali: number = 0;

  modaleAmiciAperta: boolean = false;
  vistaAttuale: 'listaAmici' | 'itinerariAmico' = 'listaAmici';
  schedaAttiva: 'amici' | 'richieste' | 'inviate' | 'cerca' = 'amici';

  listaAmici: any[] = [];
  richiesteRicevute: any[] = [];
  richiesteInviate: any[] = [];
  richiesteRifiutate: any[] = [];
  amicoSelezionato: any = null;

  usernameCercato: string = '';
  utentiTrovati: any[] = [];
  erroreRicerca: string = '';
  richiesteCompletate: string[] = [];

  isLoading: boolean = false;
  modaleLogoutAperta: boolean = false;

  constructor(
    private servAuth: AutenticazioneService,
    private amiciziaService: AmiciziaService,
    private chatService: ChatService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || '';
    if (this.isLoggato()) {
      this.caricaDatiAmicizieInBackground();

      // Sottoscrizione al centralino reattivo delle notifiche
      this.chatService.notificheTotali$.subscribe(conteggio => {
        this.notificheTotali = conteggio;
        this.cdr.detectChanges(); // Forza l'aggiornamento visivo della Navbar
      });

      // Carica lo stato iniziale dal database via REST HTTP
      this.caricaNotificheChat();
    }
  }

  caricaNotificheChat() {
    if (!this.mioUsername || this.mioUsername === 'Utente') return;

    this.chatService.ottieniNotificheTotali(this.mioUsername).subscribe({
      next: (conteggio) => {
        this.chatService.aggiornaContatoreNotifiche(conteggio);

        // 🟢 CRUCIALE: Una volta caricate le notifiche iniziali, apriamo il tubo del WebSocket globale.
        // Questo permetterà di catturare i nuovi messaggi in arrivo e aggiornare il contatore live!
        this.chatService.ascoltaNotificheGlobali(this.mioUsername);
      },
      error: (err) => console.error("Errore recupero notifiche globali chat", err)
    });
  }

  isLoggato(): boolean { return this.servAuth.isLoggato(); }
  ottieniUsername(): string | null { return this.servAuth.ottieniUsername(); }

  toggleMenu() {
    if (this.isLoading) return;
    this.mostraMenu = !this.mostraMenu;
    this.cdr.detectChanges();
  }

  logout() {
    if (this.isLoading) return;
    this.isLoading = true;
    this.mostraMenu = false;

    localStorage.clear();
    sessionStorage.clear();
    this.servAuth.esci();

    this.router.navigate(['/']).then(() => {
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }

  caricaDatiAmicizieInBackground() {
    this.amiciziaService.ottieniMieiAmici().subscribe(
      {
        next: (amici) =>
        {
          this.listaAmici = amici;
          this.cdr.detectChanges();
        },
        error: (err) => console.error("Errore recupero amici", err)
      });
    this.amiciziaService.ottieniRichiesteRicevute().subscribe(
      {
        next: (richieste) =>
        {
          this.richiesteRicevute = richieste;
          this.cdr.detectChanges();
        },
        error: (err) => console.error("Errore recupero richieste", err)
      });
    this.amiciziaService.ottieniRichiesteInviate().subscribe(
      {
        next: (inviate) =>
        {
          this.richiesteInviate = inviate;
          this.richiesteCompletate = inviate.map(r => r.riceventeUsername);
          this.cdr.detectChanges();
        },
        error: (err) => console.error("Errore recupero richieste inviate", err)
      });
    this.amiciziaService.ottieniRichiesteRifiutate().subscribe(
      {
        next: (rifiutate) =>
        {
          this.richiesteRifiutate = rifiutate;
          this.cdr.detectChanges();
        },
        error: (err) => console.error("Errore recupero richieste rifiutate", err)
      });
  }

  apriModaleAmici() {
    if (this.isLoading) return;
    this.mostraMenu = false;
    this.vistaAttuale = 'listaAmici';
    this.schedaAttiva = 'amici';
    this.pulisciRicerca();
    this.modaleAmiciAperta = true;
    this.caricaDatiAmicizieInBackground();
  }

  chiudiModale()
  {
    if (this.isLoading) return;
    this.modaleAmiciAperta = false;
    this.amicoSelezionato = null;
  }

  cambiaScheda(scheda: 'amici' | 'richieste' | 'inviate' | 'cerca')
  {
    if (this.isLoading) return;
    this.schedaAttiva = scheda;
    if (scheda === 'cerca') {
      this.pulisciRicerca();
    }
    this.caricaDatiAmicizieInBackground();
    this.cdr.detectChanges();
  }

  pulisciRicerca()
  {
    this.usernameCercato = '';
    this.utentiTrovati = [];
    this.erroreRicerca = '';
    this.cdr.detectChanges();
  }

  cercaUtente() {
    if (this.isLoading) return;
    this.erroreRicerca = '';
    this.utentiTrovati = [];

    if (!this.usernameCercato.trim()) return;
    if (this.usernameCercato.toLowerCase() === this.mioUsername.toLowerCase()) {
      this.erroreRicerca = "Non puoi cercare te stesso!";
      return;
    }

    this.isLoading = true;
    this.servAuth.ottieniDatiUtenteDalDatabase(this.usernameCercato.trim()).subscribe({
      next: (rispostaArray) => {
        if (rispostaArray && rispostaArray.length > 0) {
          this.utentiTrovati = rispostaArray.filter((utente: any) => utente.username.toLowerCase() !== this.mioUsername.toLowerCase());
          if (this.utentiTrovati.length === 0) {
            this.erroreRicerca = "Nessun altro utente trovato con questo nome.";
          }
        } else {
          this.erroreRicerca = "Nessun utente trovato con questo nome.";
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erroreRicerca = "Utente non trovato.";
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  inviaRichiestaAmicizia(riceventeUsername: string) {
    if (this.isLoading) return;
    this.isLoading = true;

    this.amiciziaService.inviaRichiesta(riceventeUsername).subscribe({
      next: () => {
        this.richiesteCompletate = [...this.richiesteCompletate, riceventeUsername];
        this.isLoading = false;
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
      },
      error: (err) => {
        this.erroreRicerca = "Esiste già una richiesta pendente o un'amicizia attiva con questo utente.";
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  haInviatoRichiesta(username: string): boolean {
    return this.richiesteCompletate.includes(username);
  }
  haRicevutoRichiesta(username: string): boolean {
    return this.richiesteRicevute.some(req => req.richiedenteUsername === username);
  }
  isRichiestaRifiutata(usernameCercato: string): boolean {
    return this.richiesteRifiutate.some(req => req.richiedenteUsername === usernameCercato || req.riceventeUsername === usernameCercato);
  }
  sonoGiaAmici(usernameCercato: string): boolean {
    return this.listaAmici.some(amico => amico.richiedenteUsername === usernameCercato || amico.riceventeUsername === usernameCercato);
  }

  accettaRichiesta(amiciziaId: number)
  {
    if (this.isLoading) return;
    this.isLoading = true;
    this.amiciziaService.accettaRichiesta(amiciziaId).subscribe({
      next: (amiciziaAggiornata) =>
      {
        this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
        this.isLoading = false;
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
      },
      error: (err) => {
        console.error("Errore nell'accettare l'amicizia", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  rifiutaRichiesta(amiciziaId: number) {
    if (this.isLoading) return;
    this.isLoading = true;

    this.amiciziaService.rifiutaRichiesta(amiciziaId).subscribe(
      {
        next: () =>
        {
          const richiestaDaRifiutare = this.richiesteRicevute.find(r => r.id === amiciziaId);
          if (richiestaDaRifiutare) {
            this.richiesteRifiutate.push(richiestaDaRifiutare);
          }
          this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
          this.isLoading = false;
          this.cdr.detectChanges();
          this.caricaDatiAmicizieInBackground();
        },
        error: (err) => {
          console.error("Errore nel rifiuto della richiesta amicizia", err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  vediItinerari(amico: any)
  {
    if (this.isLoading) return;
    this.amicoSelezionato = amico;
    this.vistaAttuale = 'itinerariAmico';
    this.cdr.detectChanges();
  }

  tornaIndietro()
  {
    if (this.isLoading) return;
    this.vistaAttuale = 'listaAmici';
    this.amicoSelezionato = null;
    this.cdr.detectChanges();
  }

  isOrganizzatore(): boolean {
    const ruolo = this.servAuth.ottieniRuolo();
    return ruolo === 'ROLE_ORGANIZZATORE' || ruolo === 'ORGANIZZATORE';
  }

  isViaggiatore(): boolean {
    return this.servAuth.ottieniRuolo() === 'ROLE_VIAGGIATORE';
  }

  apriModaleLogout() {
    if (this.isLoading) return;
    this.mostraMenu = false;
    this.modaleLogoutAperta = true;
    this.cdr.detectChanges();
  }

  annullaLogout() {
    if (this.isLoading) return;
    this.modaleLogoutAperta = false;
    this.cdr.detectChanges();
  }

  eseguiLogout() {
    if (this.isLoading) return;

    this.isLoading = true;
    this.modaleLogoutAperta = false;
    this.cdr.detectChanges();

    localStorage.clear();
    sessionStorage.clear();

    this.servAuth.esci();

    this.router.navigate(['/']).then(() => {
      console.log('Navigazione difensiva di Logout completata verso la radice.');
      this.isLoading = false;
      this.cdr.detectChanges();
    }).catch((err) => {
      console.error("Errore nel routing di Logout:", err);
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }
}
