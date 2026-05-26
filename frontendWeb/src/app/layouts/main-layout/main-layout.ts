import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AutenticazioneService } from '../../service/autenticazione.service';
import { AmiciziaService } from '../../service/amicizia.service';

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

  constructor(
    private servAuth: AutenticazioneService,
    private amiciziaService: AmiciziaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || 'Utente';
    if (this.isLoggato()) {
      this.caricaDatiAmicizieInBackground();
    }
  }

  isLoggato(): boolean { return this.servAuth.isLoggato(); }
  ottieniUsername(): string | null { return this.servAuth.ottieniUsername(); }

  toggleMenu() {
    this.mostraMenu = !this.mostraMenu;
    this.cdr.detectChanges();
  }

  logout() {
    this.mostraMenu = false;
    this.servAuth.esci();
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
    this.mostraMenu = false;
    this.vistaAttuale = 'listaAmici';
    this.schedaAttiva = 'amici';
    this.pulisciRicerca();
    this.modaleAmiciAperta = true;
    this.caricaDatiAmicizieInBackground();
  }

  chiudiModale()
  {
    this.modaleAmiciAperta = false; this.amicoSelezionato = null;
  }
  cambiaScheda(scheda: 'amici' | 'richieste' | 'inviate' | 'cerca')
  {
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
    this.erroreRicerca = ''; this.utentiTrovati = [];
    if (!this.usernameCercato.trim()) return;
    if (this.usernameCercato.toLowerCase() === this.mioUsername.toLowerCase()) { this.erroreRicerca = "Non puoi cercare te stesso!"; return; }

    this.servAuth.ottieniDatiUtenteDalDatabase(this.usernameCercato.trim()).subscribe({
      next: (rispostaArray) => {
        if (rispostaArray && rispostaArray.length > 0) {
          this.utentiTrovati = rispostaArray.filter((utente: any) => utente.username.toLowerCase() !== this.mioUsername.toLowerCase());
          if (this.utentiTrovati.length === 0) { this.erroreRicerca = "Nessun altro utente trovato con questo nome."; }
        } else { this.erroreRicerca = "Nessun utente trovato con questo nome."; }
        this.cdr.detectChanges();
      },
      error: (err) => { this.erroreRicerca = "Utente non trovato."; console.error(err); this.cdr.detectChanges(); }
    });
  }

  inviaRichiestaAmicizia(riceventeUsername: string)
  {
    this.amiciziaService.inviaRichiesta(riceventeUsername).subscribe(
      { next: () =>
        {
          this.richiesteCompletate = [...this.richiesteCompletate, riceventeUsername];
          this.cdr.detectChanges(); this.caricaDatiAmicizieInBackground();
        },
        error: (err) =>
        {
          this.erroreRicerca = "Esiste già una richiesta pendente o un'amicizia attiva con questo utente.";
          console.error(err); this.cdr.detectChanges();
        }
      });
  }
  haInviatoRichiesta(username: string): boolean
  {
    return this.richiesteCompletate.includes(username);
  }
  haRicevutoRichiesta(username: string): boolean
  {
    return this.richiesteRicevute.some(req => req.richiedenteUsername === username);
  }
  isRichiestaRifiutata(usernameCercato: string): boolean
  {
    return this.richiesteRifiutate.some(req => req.richiedenteUsername === usernameCercato || req.riceventeUsername === usernameCercato);
  }
  sonoGiaAmici(usernameCercato: string): boolean
  {
    return this.listaAmici.some(amico => amico.richiedenteUsername === usernameCercato || amico.riceventeUsername === usernameCercato);
  }

  accettaRichiesta(amiciziaId: number)
  {
    this.amiciziaService.accettaRichiesta(amiciziaId).subscribe({
      next: (amiciziaAggiornata) =>
      {
        this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
        this.cdr.detectChanges();
        this.caricaDatiAmicizieInBackground();
        },
      error: (err) => console.error("Errore nell'accettare l'amicizia", err)
    });
  }
  rifiutaRichiesta(amiciziaId: number)
  {
    this.amiciziaService.rifiutaRichiesta(amiciziaId).subscribe(
      {
        next: () =>
        { const richiestaDaRifiutare = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
          if (richiestaDaRifiutare)
          {
            this.richiesteRifiutate.push(richiestaDaRifiutare);
          }
          this.richiesteRicevute = this.richiesteRicevute.filter(r => r.id !== amiciziaId);
          this.cdr.detectChanges();
          this.caricaDatiAmicizieInBackground();
        },
        error: (err) => console.error("Errore nel rifiutare l'amicizia", err)
      });
  }

  vediItinerari(amico: any)
  {
    this.amicoSelezionato = amico;
    this.vistaAttuale = 'itinerariAmico';
    this.cdr.detectChanges();
  }
  tornaIndietro()
  {
    this.vistaAttuale = 'listaAmici';
    this.amicoSelezionato = null;
    this.cdr.detectChanges();
  }
}
