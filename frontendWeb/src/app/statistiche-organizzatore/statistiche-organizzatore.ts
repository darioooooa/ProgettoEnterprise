import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-statistiche-organizzatore',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './statistiche-organizzatore.html',
  styleUrl: './statistiche-organizzatore.css'
})
export class StatisticheOrganizzatore implements OnInit {

  totaleViaggi: number = 24;
  mediaRecensioni: number = 4.8;
  totaleRecensioni: number = 156;

  filtroGuadagni: 'SETTIMANA' | 'MESE' | 'ANNO' | 'TOTALE' = 'MESE';
  guadagnoMostrato: number = 0;

  datiGuadagni = {
    SETTIMANA: 450.50,
    MESE: 2100.00,
    ANNO: 18500.00,
    TOTALE: 32450.00
  };

  viaggiRecenti = [
    { titolo: 'Tour dell\'Andalusia', data: '12/05/2026', postiVenduti: 15, guadagno: 1200 },
    { titolo: 'Weekend a Praga', data: '28/04/2026', postiVenduti: 8, guadagno: 640 },
    { titolo: 'Settimana in Islanda', data: '10/04/2026', postiVenduti: 12, guadagno: 2400 }
  ];

  isLoading: boolean = false;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.aggiornaGuadagni();
  }

  cambiaFiltro(nuovoFiltro: 'SETTIMANA' | 'MESE' | 'ANNO' | 'TOTALE') {
    if (this.isLoading) return;

    this.isLoading = true;
    this.filtroGuadagni = nuovoFiltro;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.aggiornaGuadagni();
      this.isLoading = false;
      this.cdr.detectChanges();
    }, 400);
  }

  private aggiornaGuadagni() {
    this.guadagnoMostrato = this.datiGuadagni[this.filtroGuadagni];
  }

  getStelleArray(voto: number): number[] {
    return Array(Math.round(voto)).fill(0);
  }
}
