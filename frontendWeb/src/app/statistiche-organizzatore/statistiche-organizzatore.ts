import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViaggioService } from '../service/viaggio.service';
import { AutenticazioneService } from '../service/autenticazione.service';
import { PrenotazioneService } from '../service/prenotazione.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-statistiche-organizzatore',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './statistiche-organizzatore.html',
  styleUrl: './statistiche-organizzatore.css'
})
export class StatisticheOrganizzatore implements OnInit {

  totaleViaggi: number = 0;
  mediaRecensioni: number = 0;
  totaleRecensioni: number = 0;

  filtroGuadagni: 'SETTIMANA' | 'MESE' | 'ANNO' | 'TOTALE' = 'MESE';
  guadagnoMostrato: number = 0;

  datiGuadagni = {
    SETTIMANA: 0,
    MESE: 0,
    ANNO: 0,
    TOTALE: 0
  };

  viaggiRecenti: any[] = [];
  isLoading: boolean = true;


  mostraTuttiViaggi: boolean = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private viaggioService: ViaggioService,
    private authService: AutenticazioneService,
    private prenotazioneService: PrenotazioneService
  ) {}

  ngOnInit(): void {
    this.caricaStatisticheReali();
  }

  caricaStatisticheReali() {
    this.isLoading = true;
    const mioId = this.authService.ottieniId();

    if (!mioId) {
      console.warn("ID Organizzatore non trovato");
      this.isLoading = false;
      return;
    }

    forkJoin({
      chiamataViaggi: this.viaggioService.getViaggiByOrganizzatore(Number(mioId)),
      chiamataPrenotazioni: this.prenotazioneService.getListaPrenotazioni()
    }).subscribe({
      next: ({ chiamataViaggi, chiamataPrenotazioni }) => {

        const viaggi = Array.isArray(chiamataViaggi) ? chiamataViaggi : ((chiamataViaggi as any).content || []);
        const prenotazioni = Array.isArray(chiamataPrenotazioni) ? chiamataPrenotazioni : ((chiamataPrenotazioni as any).content || []);

        this.calcolaMetriche(viaggi, prenotazioni);

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore nel recupero dei dati per le statistiche", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  calcolaMetriche(viaggi: any[], prenotazioni: any[]) {
    this.totaleViaggi = viaggi.length;

    let sommaVotiTotale = 0;
    let totaleRec = 0;

    const mappaPrezziViaggi = new Map<number, number>();
    const mappaGuadagniViaggi = new Map<number, number>();
    const mappaPostiVenduti = new Map<number, number>();


    viaggi.forEach(v => {
      const numRec = v.numeroRecensioni || 0;
      const mediaV = v.mediaRecensioni || 0;
      totaleRec += numRec;
      sommaVotiTotale += (mediaV * numRec);

      mappaPrezziViaggi.set(v.id, v.prezzo || 0);
      mappaGuadagniViaggi.set(v.id, 0);
      mappaPostiVenduti.set(v.id, 0);
    });

    this.totaleRecensioni = totaleRec;
    this.mediaRecensioni = totaleRec > 0 ? Number((sommaVotiTotale / totaleRec).toFixed(1)) : 0;

    let guadagnoTot = 0;
    let guadagnoAnno = 0;
    let guadagnoMese = 0;
    let guadagnoSettimana = 0;

    const oggi = new Date();
    const annoCorrente = oggi.getFullYear();
    const meseCorrente = oggi.getMonth();

    const unaSettimanaFa = new Date();
    unaSettimanaFa.setDate(oggi.getDate() - 7);

    prenotazioni.forEach(p => {

      if (p.stato !== 'CONFERMATA') {
        return;
      }

      const dataStr = p.dataPagamento || p.dataPrenotazione;
      if (!dataStr) return;

      const dataP = new Date(dataStr);

      const idViaggio = p.viaggio?.id || p.viaggioId || p.idViaggio;
      const prezzoUnitario = idViaggio ? (mappaPrezziViaggi.get(idViaggio) || 0) : 0;
      const numPersone = p.numeroPersone || 1;

      const incasso = p.prezzoTotale ? p.prezzoTotale : (prezzoUnitario * numPersone);

      if (idViaggio && mappaGuadagniViaggi.has(idViaggio)) {
        mappaGuadagniViaggi.set(idViaggio, mappaGuadagniViaggi.get(idViaggio)! + incasso);
        mappaPostiVenduti.set(idViaggio, mappaPostiVenduti.get(idViaggio)! + numPersone);
      }

      guadagnoTot += incasso;

      if (dataP.getFullYear() === annoCorrente) {
        guadagnoAnno += incasso;

        if (dataP.getMonth() === meseCorrente) {
          guadagnoMese += incasso;
        }
      }

      if (dataP >= unaSettimanaFa && dataP <= oggi) {
        guadagnoSettimana += incasso;
      }
    });

    this.datiGuadagni = {
      SETTIMANA: guadagnoSettimana,
      MESE: guadagnoMese,
      ANNO: guadagnoAnno,
      TOTALE: guadagnoTot
    };

    let listaViaggiPerTabella: any[] = [];
    viaggi.forEach(v => {
      if (v.dataInizio) {
        listaViaggiPerTabella.push({
          titolo: v.titolo,
          data: v.dataInizio,
          postiVenduti: mappaPostiVenduti.get(v.id) || 0,
          guadagno: mappaGuadagniViaggi.get(v.id) || 0,
          dataOggetto: new Date(v.dataInizio)
        });
      }
    });

    listaViaggiPerTabella.sort((a, b) => {
      if (b.guadagno !== a.guadagno) {
        return b.guadagno - a.guadagno;
      }
      return b.dataOggetto.getTime() - a.dataOggetto.getTime();
    });

    this.viaggiRecenti = listaViaggiPerTabella;

    this.aggiornaGuadagni();
  }

  cambiaFiltro(nuovoFiltro: 'SETTIMANA' | 'MESE' | 'ANNO' | 'TOTALE') {
    if (this.isLoading) return;
    this.filtroGuadagni = nuovoFiltro;
    this.aggiornaGuadagni();
    this.cdr.detectChanges();
  }

  private aggiornaGuadagni() {
    this.guadagnoMostrato = this.datiGuadagni[this.filtroGuadagni];
  }

  getStelleArray(voto: number): number[] {
    return Array(Math.round(voto)).fill(0);
  }

  apriTuttiViaggi() {
    this.mostraTuttiViaggi = true;
  }

  chiudiTuttiViaggi() {
    this.mostraTuttiViaggi = false;
  }

  get primiCinqueViaggi() {
    return this.viaggiRecenti.slice(0, 5);
  }
  get totalePostiVenduti(): number {
    return this.viaggiRecenti.reduce((sum, v) => sum + (v.postiVenduti || 0), 0);
  }

  get totaleRicavo(): number {
    return this.viaggiRecenti.reduce((sum, v) => sum + (v.guadagno || 0), 0);
  }
}
