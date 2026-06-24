import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, RouterLink, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AutenticazioneService } from '../../service/autenticazione.service';
import { AmiciziaService } from '../../service/amicizia.service';
import { ChatService } from '../../service/chat.service';
import { ItinerarioService } from '../../service/itinerario.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, FormsModule],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  mostraMenu: boolean = false;
  mioUsername: string = '';
  notificheTotali: number = 0;
  public invitiPendentiCount: number = 0;

  modaleAmiciAperta: boolean = false;
  vistaAttuale: 'listaAmici' | 'itinerariAmico' = 'listaAmici';
  schedaAttiva: 'amici' | 'richieste' | 'inviate' | 'cerca' = 'amici';

  listaAmici: any[] = [];
  richiesteRicevute: any[] = [];
  richiesteInviate: any[] = [];
  richiesteRifiutate: any[] = [];
  amicoSelezionato: any = null;
  itinerariAmicoSelezionato: any[] = [];

  usernameCercato: string = '';
  utentiTrovati: any[] = [];
  erroreRicerca: string = '';
  richiesteCompletate: string[] = [];

  isLoading: boolean = false;
  modaleLogoutAperta: boolean = false;
  numeroRichiestePendenti: number = 0;

  private controlloAutomatico?: Subscription;
  private controlloPagine?: Subscription;

  modaleConfermaAperta: boolean = false;
  messaggioConferma: string = '';
  azioneDaConfermare: (() => void) | null = null;

  constructor(
    private servAuth: AutenticazioneService,
    private amiciziaService: AmiciziaService,
    private chatService: ChatService,
    private itinerarioService: ItinerarioService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || '';
    if (this.isLoggato()) {
      this.caricaDatiAmicizieInBackground();
      this.avviaControlloAmicizie();

      this.chatService.notificheTotali$.subscribe(conteggio => {
        this.notificheTotali = conteggio;
        this.cdr.detectChanges();
      });
      this.caricaNotificheChat();
      this.caricaNotificheCondivisione()
      //per tenere la navbar in ascolto
      this.itinerarioService.aggiornaNotifiche.subscribe(() => {
        this.caricaNotificheCondivisione();
      });
    }

    this.controlloPagine = this.router.events.subscribe(evento => {
      if (evento instanceof NavigationEnd) {
        if (!evento.urlAfterRedirects.includes('/viaggi/')) {
          const appuntiAmico = sessionStorage.getItem('amicoDaRipristinare');

          if (appuntiAmico) {
            const amico = JSON.parse(appuntiAmico);
            sessionStorage.removeItem('amicoDaRipristinare');

            this.modaleAmiciAperta = true;
            this.vediItinerari(amico);
          }
        }
      }
    });
  }

  ngOnDestroy() {
    if (this.controlloAutomatico) {
      this.controlloAutomatico.unsubscribe();
    }
    if (this.controlloPagine) {
      this.controlloPagine.unsubscribe();
    }
  }

  caricaNotificheChat() {
    if (!this.mioUsername || this.mioUsername === 'Utente') return;

    this.chatService.ottieniNotificheTotali(this.mioUsername).subscribe({
      next: (conteggio) => {
        this.chatService.aggiornaContatoreNotifiche(conteggio);
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

  avviaControlloAmicizie() {
    if (!this.mioUsername) return;
    this.chatService.ascoltaNotificheAmicizia(this.mioUsername);

    this.controlloAutomatico = this.chatService.notificheAmicizia$.subscribe(() => {
      this.amiciziaService.ottieniRichiesteRicevute().subscribe({
        next: (richieste) => {
          this.richiesteRicevute = richieste;
          this.numeroRichiestePendenti = richieste.length;
          this.cdr.detectChanges();
        }
      });

    });
  }

  caricaDatiAmicizieInBackground() {
    this.amiciziaService.ottieniMieiAmici().subscribe({
      next: (amici) => {
        this.listaAmici = amici;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore recupero amici", err)
    });
    this.amiciziaService.ottieniRichiesteRicevute().subscribe({
      next: (richieste) => {
        this.richiesteRicevute = richieste;
        this.numeroRichiestePendenti = richieste.length;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore recupero richieste", err)
    });
    this.amiciziaService.ottieniRichiesteInviate().subscribe({
      next: (inviate) => {
        this.richiesteInviate = inviate;
        this.richiesteCompletate = inviate.map(r => r.riceventeUsername);
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore recupero richieste inviate", err)
    });
    this.amiciziaService.ottieniRichiesteRifiutate().subscribe({
      next: (rifiutate) => {
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

  chiudiModale() {
    if (this.isLoading) return;
    this.modaleAmiciAperta = false;
    this.amicoSelezionato = null;
  }

  cambiaScheda(scheda: 'amici' | 'richieste' | 'inviate' | 'cerca') {
    if (this.isLoading) return;
    this.schedaAttiva = scheda;

    if (scheda === 'cerca') {
      this.pulisciRicerca();
    } else {
      this.caricaDatiAmicizieInBackground();
    }
    this.cdr.detectChanges();
  }

  pulisciRicerca() {
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

  chiediConferma(messaggio: string, azione: () => void) {
    if (this.isLoading) return;
    this.messaggioConferma = messaggio;
    this.azioneDaConfermare = azione;
    this.modaleConfermaAperta = true;
    this.cdr.detectChanges();
  }

  eseguiConferma() {
    if (this.azioneDaConfermare) {
      this.azioneDaConfermare();
    }
    this.chiudiConferma();
  }

  chiudiConferma() {
    this.modaleConfermaAperta = false;
    this.azioneDaConfermare = null;
    this.cdr.detectChanges();
  }

  inviaRichiestaAmicizia(riceventeUsername: string) {
    this.chiediConferma(`Vuoi inviare una richiesta a ${riceventeUsername}?`, () => {
      this.isLoading = true;
      this.amiciziaService.inviaRichiesta(riceventeUsername).subscribe({
        next: () => {
          this.richiesteCompletate = [...this.richiesteCompletate, riceventeUsername];
          this.isLoading = false;
          this.caricaDatiAmicizieInBackground();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.erroreRicerca = "Esiste già una richiesta pendente o un'amicizia attiva con questo utente.";
          console.error(err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    });
  }

  accettaRichiesta(amiciziaId: number) {
    this.chiediConferma('Vuoi accettare questa richiesta di amicizia?', () => {
      this.isLoading = true;
      this.amiciziaService.accettaRichiesta(amiciziaId).subscribe({
        next: (amiciziaAggiornata) => {
          this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
          this.isLoading = false;
          this.caricaDatiAmicizieInBackground();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore nell'accettare l'amicizia", err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    });
  }

  rifiutaRichiesta(amiciziaId: number) {
    this.chiediConferma('Sei sicuro di voler rifiutare questa richiesta?', () => {
      this.isLoading = true;
      this.amiciziaService.rifiutaRichiesta(amiciziaId).subscribe({
        next: () => {
          const richiestaDaRifiutare = this.richiesteRicevute.find(r => r.id === amiciziaId);
          if (richiestaDaRifiutare) {
            this.richiesteRifiutate.push(richiestaDaRifiutare);
          }
          this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
          this.isLoading = false;
          this.caricaDatiAmicizieInBackground();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore nel rifiutare l'amicizia", err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
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

  ottieniUsernameAmico(amico: any): string {
    if (!amico) return '';
    return amico.richiedenteUsername === this.mioUsername ? amico.riceventeUsername : amico.richiedenteUsername;
  }

  vediItinerari(amico: any) {
    if (this.isLoading) return;
    this.amicoSelezionato = amico;
    this.vistaAttuale = 'itinerariAmico';
    this.itinerariAmicoSelezionato = [];
    this.isLoading = true;
    this.cdr.detectChanges();

    const usernameAmico = this.ottieniUsernameAmico(amico);

    this.itinerarioService.getListePubblicheUtente(usernameAmico).subscribe({
      next: (liste) => {
        this.itinerariAmicoSelezionato = liste;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore recupero itinerari amico", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  vaiAlDettaglioViaggio(viaggioId: number) {
    if (this.isLoading) return;

    if (this.amicoSelezionato) {
      sessionStorage.setItem('amicoDaRipristinare', JSON.stringify(this.amicoSelezionato));
    }

    this.chiudiModale();
    this.router.navigate(['/viaggi', viaggioId]);
  }

  tornaIndietro() {
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
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }
  isSezioneItinerari(): boolean {
    return this.router.url.includes('/miei-itinerari') || this.router.url.includes('/richieste-condivisione-itinerari');
  }
  caricaNotificheCondivisione() {
    if (this.isLoggato() && this.isViaggiatore()) {
      this.itinerarioService.getInvitiInSospeso().subscribe({
        next: (data) => {
          this.invitiPendentiCount = data.length; // Prende quante richieste ci sono in array
        },
        error: (err) => console.error("Errore recupero notifiche condivisione", err)
      });
    }
  }
}
