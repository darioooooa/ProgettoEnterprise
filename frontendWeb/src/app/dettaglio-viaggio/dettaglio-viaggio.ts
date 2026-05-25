import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ViaggioService } from '../service/viaggio.service';
import { AutenticazioneService } from '../service/autenticazione.service';

import { GalleriaComponent } from './components/galleria/galleria';
import { ProgrammaComponent } from './components/programma/programma';
import { CommunityComponent } from './components/community/community';

@Component({
  selector: 'app-dettaglio-viaggio',
  standalone: true,
  imports: [
    CommonModule,
    GalleriaComponent,
    ProgrammaComponent,
    CommunityComponent
  ],
  templateUrl: './dettaglio-viaggio.html',
  styleUrl: './dettaglio-viaggio.css'
})
export class DettaglioViaggio implements OnInit {
  viaggioId!: number;
  statistiche: any = null;
  organizzatoreUsername: string = '';
  mioUsername: string = '';

  // Stato del tab attivo
  tabAttivo: 'galleria' | 'programma' | 'community' = 'programma';

  constructor(
    private route: ActivatedRoute,
    private viaggioService: ViaggioService,
    private servAuth: AutenticazioneService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.mioUsername = this.servAuth.ottieniUsername() || '';
    this.viaggioId = Number(this.route.snapshot.paramMap.get('id'));

    if (this.viaggioId) {
      this.caricaStatistichePadre();
    }
  }

  isLoggato(): boolean { return this.servAuth.isLoggato(); }
  ottieniRuolo(): string | null { return this.servAuth.ottieniRuolo(); }

  isMioViaggio(): boolean {
    if (!this.mioUsername || !this.organizzatoreUsername) return false;
    return this.isLoggato() &&
      this.ottieniRuolo() === 'ROLE_ORGANIZZATORE' &&
      this.mioUsername.trim().toLowerCase() === this.organizzatoreUsername.trim().toLowerCase();
  }

  caricaStatistichePadre() {
    this.viaggioService.getStatisticheRecensioni(this.viaggioId).subscribe({
      next: (stats) => {
        this.statistiche = stats;
        this.organizzatoreUsername = stats.organizzatoreUsername || '';
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore nel caricamento delle statistiche padre:", err)
    });
  }

  // Invocato tramite @Output dai figli se un'azione richiede di aggiornare i voti in cima
  onSincronizzaRichiesta() {
    this.caricaStatistichePadre();
  }
}
