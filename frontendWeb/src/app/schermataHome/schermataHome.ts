import { Component, OnInit, ChangeDetectorRef, NgZone, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutenticazioneService } from '../service/autenticazione.service';
import { AmiciziaService } from '../service/amicizia.service';
import { ViaggioService } from '../service/viaggio.service';
import { ItinerarioService } from '../service/itinerario.service';

@Component({
  selector: 'app-schermata-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './schermataHome.html',
  styleUrl: './schermataHome.css'
})
export class SchermataHomeComponent implements OnInit {
  titolo = 'Benvenuti in TravelBooking';

  filtriViaggio = {
    destinazione: '',
    dataInizioMin: '',
    maxPartecipanti: ''
  };
  viaggiTrovati: any[] = [];
  haCercato = false;

  paginaCorrente = 0;
  totalePagine = 0;


  openItineraryMenuId: number | null = null;
  mieiItinerari: any[] = [];
  nomeNuovoItinerario: string = ''; // Variabile per l'input del nuovo itinerario

  listaAmici: any[] = [];
  richiesteRicevute: any[] = [];
  richiesteInviate: any[] = [];
  richiesteRifiutate: any[] = [];
  mioUsername: string = '';
  modaleItinerarioAperta = false;

  richiesteCompletate: string[] = [];

  constructor(
    private servAuth: AutenticazioneService,
    private amiciziaService: AmiciziaService,
    private cdr: ChangeDetectorRef,
    private viaggioService: ViaggioService,
    private zone: NgZone,
    private itinerarioService: ItinerarioService
  ) {
    console.log('Schermata Home inizializzata!');
  }

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || 'Utente';
    if (this.isLoggato()) {
      this.caricaDatiAmicizieInBackground();
      this.caricaItinerariUtente();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (this.openItineraryMenuId !== null &&
      !target.closest('.card') &&
      !target.closest('.itinerary-dropdown')) {
      this.openItineraryMenuId = null;
    }
  }

  isLoggato(): boolean { return this.servAuth.isLoggato(); }

  caricaItinerariUtente() {
    this.itinerarioService.getMieListe().subscribe({
      next: (liste) => {
        this.mieiItinerari = liste;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Ops, errore nel caricamento degli itinerari", err)
    });
  }

  caricaDatiAmicizieInBackground() {
    this.amiciziaService.ottieniMieiAmici().subscribe({ next: (amici) => { this.listaAmici = amici; this.cdr.detectChanges(); }, error: (err) => console.error("Errore recupero amici", err) });
    this.amiciziaService.ottieniRichiesteRicevute().subscribe({ next: (richieste) => { this.richiesteRicevute = richieste; this.cdr.detectChanges(); }, error: (err) => console.error("Errore recupero richieste", err) });
    this.amiciziaService.ottieniRichiesteInviate().subscribe({ next: (inviate) => { this.richiesteInviate = inviate; this.richiesteCompletate = inviate.map(r => r.riceventeUsername); this.cdr.detectChanges(); }, error: (err) => console.error("Errore recupero richieste inviate", err) });
    this.amiciziaService.ottieniRichiesteRifiutate().subscribe({ next: (rifiutate) => { this.richiesteRifiutate = rifiutate; this.cdr.detectChanges(); }, error: (err) => console.error("Errore recupero richieste rifiutate", err) });
  }

  avviaRicercaViaggi(evento?: Event) {
    if (evento) { evento.preventDefault(); }
    this.haCercato = true;
    this.paginaCorrente = 0;
    this.eseguiRicerca();
  }

  eseguiRicerca() {
    const filtriPuliti: any = {};
    if (this.filtriViaggio.destinazione) filtriPuliti.destinazione = this.filtriViaggio.destinazione;
    if (this.filtriViaggio.dataInizioMin) filtriPuliti.dataInizioMin = this.filtriViaggio.dataInizioMin + 'T00:00:00';
    if (this.filtriViaggio.maxPartecipanti) filtriPuliti.maxPartecipanti = this.filtriViaggio.maxPartecipanti;

    this.zone.run(() => {
      this.viaggioService.getViaggi(this.paginaCorrente, filtriPuliti).subscribe({
        next: (risposta) => {
          //console.log("Ecco cosa contiene un singolo viaggio:", risposta.content[0]);
          this.viaggiTrovati = risposta.content ? [...risposta.content] : [];
          this.totalePagine = risposta.totalPages || 0;
          this.openItineraryMenuId = null;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Errore durante la ricerca viaggi', err)
      });
    });
  }

  paginaPrecedente() { if (this.paginaCorrente > 0) { this.paginaCorrente--; this.eseguiRicerca(); window.scrollTo({ top: 500, behavior: 'smooth' }); } }
  paginaSuccessiva() { if (this.paginaCorrente < this.totalePagine - 1) { this.paginaCorrente++; this.eseguiRicerca(); window.scrollTo({ top: 500, behavior: 'smooth' }); } }


  toggleItineraryMenu(viaggioId: number, event: Event) {
    event.stopPropagation();
    if (this.openItineraryMenuId === viaggioId) {
      this.openItineraryMenuId = null;
    } else {
      this.openItineraryMenuId = viaggioId;
      this.nomeNuovoItinerario = '';
    }
  }

  creaNuovoItinerario(event: Event) {
    event.stopPropagation();
    if (!this.nomeNuovoItinerario.trim()) {
      alert("Scrivi un nome per il tuo nuovo itinerario!");
      return;
    }

    const nuovaLista = {
      nome: this.nomeNuovoItinerario.trim(),
      visibilita: 'PRIVATA'
    };

    this.itinerarioService.creaLista(nuovaLista).subscribe({
      next: (risposta) => {
        this.mieiItinerari.push(risposta);
        this.nomeNuovoItinerario = ''; // Svuotiamo la casella di testo
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante la creazione", err);
        alert("Ops, c'è stato un errore durante la creazione dell'itinerario.");
      }
    });
  }

  selezionaItinerario(itinerarioId: number, viaggioId: number) {
    this.openItineraryMenuId = null;
    this.itinerarioService.aggiungiViaggioAItinerario(itinerarioId, viaggioId).subscribe({
      next: (risposta: any) => {
        alert(risposta.message || "Viaggio aggiunto con successo all'itinerario!");
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio", err);
        alert("Si è verificato un problema durante l'aggiunta del viaggio.");
      }
    });
  }
  richiediNomeNuovoItinerario(viaggioId: number) {
    const nome = window.prompt("Come vuoi chiamare il nuovo itinerario?");

    if (nome && nome.trim() !== "") {
      const nuovaLista = {
        nome: nome.trim(),
        visibilita: 'PRIVATA'
      };

      this.itinerarioService.creaLista(nuovaLista).subscribe({
        next: (risposta) => {
          this.mieiItinerari.push(risposta);
          this.cdr.detectChanges();
          alert("Itinerario creato!");
        },
        error: (err) => alert("Errore nella creazione.")
      });
    }
  }
  apriModaleItinerario() {
    this.nomeNuovoItinerario = '';
    this.modaleItinerarioAperta = true;
  }

  chiudiModaleItinerario() {
    this.modaleItinerarioAperta = false;
  }

  // Aggiungi questa variabile nella classe
  isSalvataggioInCorso = false;

// Aggiorna il metodo salvaNuovoItinerario
  salvaNuovoItinerario() {
    if (!this.nomeNuovoItinerario.trim() || this.isSalvataggioInCorso) return;

    this.isSalvataggioInCorso = true; // Blocca il bottone

    this.itinerarioService.creaLista({
      nome: this.nomeNuovoItinerario.trim(),
      visibilita: 'PRIVATA'
    }).subscribe({
      next: (nuovaLista) => {
        this.mieiItinerari.push(nuovaLista);
        this.isSalvataggioInCorso = false;
        this.chiudiModaleItinerario();

        alert("Itinerario creato con successo!");


        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isSalvataggioInCorso = false;
        alert("Errore durante la creazione. Riprova.");
        this.cdr.detectChanges();
      }
    });
  }
}
