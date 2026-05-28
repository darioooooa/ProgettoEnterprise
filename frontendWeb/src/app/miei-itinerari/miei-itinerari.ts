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

  public menuSpostaAperto: { [key: string]: boolean } = {};

  public mostraModaleCreazione = false;
  public nuovoNome = '';
  public nuovaVisibilita = 'PRIVATA';

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
    if (confirm('Sei sicuro di voler eliminare tutto questo itinerario?')) {
      this.itinerarioService.eliminaLista(idItinerario).subscribe({
        next: () => {
          this.itinerari = this.itinerari.filter(it => it.idItinerario !== idItinerario);
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err)
      });
    }
  }

  rimuoviViaggio(idItinerario: number, idViaggio: number) {
    if (confirm('Vuoi togliere questo viaggio dalla lista?')) {
      this.itinerarioService.rimuoviViaggio(idItinerario, idViaggio).subscribe({
        next: () => {
          const itinerario = this.itinerari.find(it => it.idItinerario === idItinerario);
          if (itinerario) {
            itinerario.viaggiContenuti = itinerario.viaggiContenuti.filter((v: any) => v.id !== idViaggio);
          }
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err)
      });
    }
  }

  toggleMenuSposta(idItinerario: number, idViaggio: number) {
    const chiave = `${idItinerario}-${idViaggio}`;
    this.menuSpostaAperto[chiave] = !this.menuSpostaAperto[chiave];
  }

  eseguiSpostamento(idSorgente: number, idDestinazione: number, idViaggio: number) {
    this.itinerarioService.spostaViaggio(idSorgente, idDestinazione, idViaggio).subscribe({
      next: () => {
        this.menuSpostaAperto[`${idSorgente}-${idViaggio}`] = false;
        this.caricaItinerari();
      },
      error: (err) => {
        console.error("Errore durante lo spostamento del viaggio", err);
      }
    });
  }

  apriModaleCreazione() {
    this.mostraModaleCreazione = true;
    this.nuovoNome = '';
    this.nuovaVisibilita = 'PRIVATA';
  }

  chiudiModaleCreazione() {
    this.mostraModaleCreazione = false;
  }

  salvaNuovoItinerario() {
    if (!this.nuovoNome.trim()) {
      alert("Inserisci un nome per l'itinerario!");
      return;
    }

    const pacchettoDati = {
      nome: this.nuovoNome,
      visibilita: this.nuovaVisibilita
    };

    this.itinerarioService.creaLista(pacchettoDati).subscribe({
      next: () => {
        this.chiudiModaleCreazione();
        this.caricaItinerari();
      },
      error: (err) => {
        console.error("Errore durante la creazione", err);
      }
    });
  }

  cambiaVisibilita(itinerario: any) {
    const prossimaVisibilita = itinerario.visibilita === 'PRIVATA' ? 'PUBBLICA' : 'PRIVATA';

    if (confirm(`Vuoi davvero rendere questa lista ${prossimaVisibilita.toLowerCase()}?`)) {
      this.itinerarioService.cambiaVisibilita(itinerario.idItinerario, prossimaVisibilita).subscribe({
        next: () => {
          itinerario.visibilita = prossimaVisibilita;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore durante il cambio di visibilità", err);
        }
      });
    }
  }
}
