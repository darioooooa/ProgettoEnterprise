import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ItinerarioService } from '../service/itinerario.service';
import { AutenticazioneService } from '../service/autenticazione.service';

@Component({
  selector: 'app-miei-itinerari',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './miei-itinerari.html',
  styleUrl: './miei-itinerari.css'
})
export class MieiItinerari implements OnInit {
  public itinerari: any[] = [];
  public itinerariCondivisi: any[] = [];

  public menuSpostaAperto: { [key: string]: boolean } = {};

  public mostraModaleCreazione = false;
  public nuovoNome = '';
  public nuovaVisibilita = 'PRIVATA';

  public mostraModaleCondivisione= false
  public emailDaInvitare='';
  public idItinerarioDaCondividere: number | null = null;


  public isLoading: boolean = false;

  constructor(
    private itinerarioService: ItinerarioService,
    private cdr: ChangeDetectorRef,
    private authService: AutenticazioneService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isLoggato()) {
      this.router.navigate(['/login']);
      return;
    }
    this.caricaItinerari();
    this.caricaItinerariCondivisi();
  }

  caricaItinerari() {
    this.itinerarioService.getMieListe().subscribe({
      next: (data) => {
        this.itinerari = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Non è stato possibile caricare gli itinerari", err);
      }
    });
  }

  eliminaItinerario(idItinerario: number) {
    if (this.isLoading) return;

    if (confirm('Sei sicuro di voler eliminare tutto questo itinerario?')) {
      this.isLoading = true;
      this.itinerarioService.eliminaLista(idItinerario).subscribe({
        next: () => {
          this.itinerari = this.itinerari.filter(it => it.idItinerario !== idItinerario);
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  rimuoviViaggio(idItinerario: number, idViaggio: number) {
    if (this.isLoading) return;

    if (confirm('Vuoi togliere questo viaggio dalla lista?')) {
      this.isLoading = true;
      this.itinerarioService.rimuoviViaggio(idItinerario, idViaggio).subscribe({
        next: () => {
          const itinerario = this.itinerari.find(it => it.idItinerario === idItinerario);
          if (itinerario) {
            itinerario.viaggiContenuti = itinerario.viaggiContenuti.filter((v: any) => v.id !== idViaggio);
          }
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  toggleMenuSposta(idItinerario: number, idViaggio: number) {
    if (this.isLoading) return;
    const chiave = `${idItinerario}-${idViaggio}`;
    this.menuSpostaAperto[chiave] = !this.menuSpostaAperto[chiave];
  }

  eseguiSpostamento(idSorgente: number, idDestinazione: number, idViaggio: number) {
    if (this.isLoading) return;
    this.isLoading = true;

    this.itinerarioService.spostaViaggio(idSorgente, idDestinazione, idViaggio).subscribe({
      next: () => {
        this.menuSpostaAperto[`${idSorgente}-${idViaggio}`] = false;
        this.itinerarioService.getMieListe().subscribe({
          next: (data) => {
            this.itinerari = data;
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: () => { this.isLoading = false; this.cdr.detectChanges(); }
        });
      },
      error: (err) => {
        console.error("Errore durante lo spostamento del viaggio", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  apriModaleCreazione() {
    if (this.isLoading) return;
    this.mostraModaleCreazione = true;
    this.nuovoNome = '';
    this.nuovaVisibilita = 'PRIVATA';
  }

  chiudiModaleCreazione() {
    this.mostraModaleCreazione = false;
    this.nuovoNome = '';
    this.nuovaVisibilita = 'PRIVATA';

    // Forza la grafica ad aggiornarsi e far sparire la modale
    this.cdr.detectChanges();
  }

  salvaNuovoItinerario() {
    if (this.isLoading) return;

    if (!this.nuovoNome.trim()) {
      alert("Inserisci un nome per l'itinerario!");
      return;
    }

    this.isLoading = true;
    const pacchettoDati = {
      nome: this.nuovoNome,
      visibilita: this.nuovaVisibilita,
      inCondivisione:false
    };

    this.itinerarioService.creaLista(pacchettoDati).subscribe({
      next: () => {
        this.chiudiModaleCreazione();
        this.itinerarioService.getMieListe().subscribe({
          next: (data) => {
            this.itinerari = data;
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: () => { this.isLoading = false; this.cdr.detectChanges(); }
        });
      },
      error: (err) => {
        console.error("Errore durante la creazione", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  cambiaVisibilita(itinerario: any) {
    if (this.isLoading) return;
    const prossimaVisibilita = itinerario.visibilita === 'PRIVATA' ? 'PUBBLICA' : 'PRIVATA';

    if (confirm(`Vuoi davvero rendere questa lista ${prossimaVisibilita.toLowerCase()}?`)) {
      this.isLoading = true;
      this.itinerarioService.cambiaVisibilita(itinerario.idItinerario, prossimaVisibilita).subscribe({
        next: () => {
          itinerario.visibilita = prossimaVisibilita;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore durante il cambio di visibilità", err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }
  //METODI PER LA CONDIVISIONE DEGLI ITINERARI
  apriModaleCondivisione(idItinerario: number) {
    if (this.isLoading) return;
    this.idItinerarioDaCondividere = idItinerario;
    this.emailDaInvitare = '';
    this.mostraModaleCondivisione = true;
  }

  chiudiModaleCondivisione() {
    if (this.isLoading) return;
    this.mostraModaleCondivisione = false;
    this.idItinerarioDaCondividere = null;
  }

  inviaInvitoCondivisione() {
    if (this.isLoading) return;
    if (!this.emailDaInvitare.trim()) {
      alert("Inserisci l'email dell'amico da invitare!");
      return;
    }

    if (this.idItinerarioDaCondividere !== null) {
      this.isLoading = true;

      this.itinerarioService.invitaCollaboratore(this.idItinerarioDaCondividere, this.emailDaInvitare).subscribe({
        next: (response) => {
          alert('Invito inviato con successo a ' + this.emailDaInvitare);
          this.chiudiModaleCondivisione();
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore durante l'invito", err);
          alert(err.error?.message || "Errore: impossibile invitare questo utente. Controlla che l'email sia corretta e che non sia già stato invitato.");
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }
  caricaItinerariCondivisi() {
    this.itinerarioService.getItinerariCondivisiConMe().subscribe({
      next: (data) => {
        this.itinerariCondivisi = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore recupero itinerari condivisi", err)
    });
  }
}
