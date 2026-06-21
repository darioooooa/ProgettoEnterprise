import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink,Router} from '@angular/router';
import {ViaggioService} from '../service/viaggio.service';
import { AutenticazioneService } from '../service/autenticazione.service';

import {MapComponent} from '../map/map';
import { PrenotazioneService } from '../service/prenotazione.service';
import { ModaleSegnalazione } from '../modale-segnalazione/modale-segnalazione';

@Component({
  selector: 'app-schermata-organizzatore',
  standalone: true,
  imports: [CommonModule, RouterLink, MapComponent,ModaleSegnalazione],
  templateUrl: './schermataOrganizzatore.html',
  styleUrl: './schermataOrganizzatore.css'
})
export class SchermataOrganizzatoreComponent implements OnInit {

  listaDeiViaggi: any[] = [];
  paginaCorrente: number = 0;
  totalePagine: number = 0;
  ultimePrenotazioni: any[] = [];

  isLoading: boolean = false;

  mostraSegnalazione = false;
  idDaSegnalare = 0;

  constructor(
    private viaggioService: ViaggioService,
    private prenotazioneService: PrenotazioneService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private servAuth: AutenticazioneService
  ) {
  }

  ngOnInit(): void {
    this.caricaViaggi();
    this.caricaPrenotazioniRicevute();
  }

  caricaViaggi(pagina: number = 0) {
    this.isLoading = true;
    this.paginaCorrente = pagina;

    const mioId = this.servAuth.ottieniId();
    const filtriDashboard = {
      organizzatoreId: mioId
    };

    this.viaggioService.getViaggi(this.paginaCorrente, filtriDashboard).subscribe({
      next: (data) => {
        this.listaDeiViaggi = data.content;
        this.totalePagine = data.totalPages;
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('Viaggi caricati:', data);
      },
      error: (e) => {
        console.error('Errore durante il caricamento:', e);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  vaiAlDettaglioViaggio(viaggio: any) {
    if (this.isLoading) return;
    const viaggioId = viaggio.id || viaggio.idViaggio;
    console.log("Navigazione da tabella al dettaglio del viaggio id:", viaggioId);
    this.router.navigate(['/viaggi', viaggioId]);
  }

  // Calcolo dello stato di svolgimento del viaggio
  getBadgeStatoSvolgimento(viaggio: any): { testo: string, classe: string } {
    if (!viaggio.dataInizio || !viaggio.dataFine) {
      return { testo: 'Dati non validi', classe: 'badge badge-grigio' };
    }

    const oggi = new Date();
    // Azzera l'orario per fare un confronto preciso sul giorno solare
    oggi.setHours(0, 0, 0, 0);

    const inizio = new Date(viaggio.dataInizio);
    inizio.setHours(0, 0, 0, 0);

    const fine = new Date(viaggio.dataFine);
    fine.setHours(0, 0, 0, 0);

    if (oggi < inizio) {
      return { testo: '📅 Non ancora iniziato', classe: 'badge badge-futuro' };
    } else if (oggi >= inizio && oggi <= fine) {
      return { testo: '✈️ In Corso', classe: 'badge badge-in-corso' };
    } else {
      return { testo: '🏁 Completato', classe: 'badge badge-completato' };
    }
  }

  paginaSuccessiva() {
    if (this.isLoading) return;
    if (this.paginaCorrente < this.totalePagine - 1 && !this.isLoading) {
      this.caricaViaggi(this.paginaCorrente + 1);
    }
  }

  paginaPrecedente() {
    if (this.isLoading) return;
    if (this.paginaCorrente > 0 && !this.isLoading) {
      this.caricaViaggi(this.paginaCorrente - 1);
    }
  }

  vaiAiDettagli(datiDallaMappa: any) {
    if (this.isLoading) return;

    if (Array.isArray(datiDallaMappa)) {
      console.log("📍 Marker multiplo cliccato!");

      // Salviamo i dati completi in memoria
      this.viaggioService.setViaggiSelezionati(datiDallaMappa);

      // Passiamo gli ID nell'URL in caso non funzioni
      const listaIds = datiDallaMappa.map(v => v.id || v.idViaggio);
      this.router.navigate(['/lista-viaggi-marker'], {
        queryParams: { ids: listaIds.join(',') }
      });
    } else {
      const viaggioId = datiDallaMappa.id || datiDallaMappa.idViaggio;
      console.log("📍 Marker singolo cliccato! Navigo ai dettagli del viaggio ID:", viaggioId);

      this.router.navigate(['/viaggi', viaggioId]);
    }
  }

  caricaPrenotazioniRicevute() {
    this.prenotazioneService.getListaPrenotazioni().subscribe({
      next: (data: any) => {
        this.ultimePrenotazioni = data.content ? data.content.slice(0, 5) : [];
        console.log("Dati veri delle prenotazioni:", this.ultimePrenotazioni);
        this.cdr.detectChanges();
      },
      error: (e) => console.error('Errore nel caricamento delle prenotazioni:', e)
    });
  }

  getIniziali(nome: string): string {
    if (!nome) return 'US';
    return nome.substring(0, 2).toUpperCase();
  }

  getColoreAvatar(nome: string): string {
    if (!nome) return 'green';
    const colori = ['green', 'purple', 'blue', 'orange'];
    return colori[nome.length % colori.length];
  }

  apriSegnalazioneViaggiatore(id: number, username: string) {
    if (this.isLoading) return;
    this.idDaSegnalare = id;
    this.mostraSegnalazione = true;
  }
}
