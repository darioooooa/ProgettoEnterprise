import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ViaggioService} from '../service/viaggio.service';
import {forkJoin} from 'rxjs';
import { Location } from '@angular/common';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-lista-viaggi-marker',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './lista-viaggi-marker.html',
  styleUrl: './lista-viaggi-marker.css',
})
export class ListaViaggiMarker implements OnInit{
  viaggi: any[] = [];
  constructor(private rotta:ActivatedRoute,
              private router:Router,
              private location: Location,
              private viaggioService: ViaggioService,
  ) {}
  ngOnInit(): void {
    this.rotta.queryParams.subscribe(params => {
      if (params['ids']) {
        const listaIds = params['ids'].split(',');
        this.caricaViaggi(listaIds);
      }
    });
  }

  private caricaViaggi(listaIds: any) {
    const chiamate = listaIds.map((id: any) => this.viaggioService.getViaggioById(Number(id)));

    // 3. forkJoin le esegue tutte insieme e aspetta che finiscano
    forkJoin(chiamate).subscribe({
      next: (risposte: any) => {
        this.viaggi = risposte;
        console.log("🎒 Viaggi caricati con successo:", this.viaggi);
      },
      error: (err) => console.error("Errore nel recupero dei viaggi:", err)
    });
  }
  tornaIndietro() {
    this.location.back();
  }

  vaiAiDettagli(viaggioId: number) {

    console.log("Navigo verso i dettagli del viaggio:", viaggioId);
    this.router.navigate(['/viaggi', viaggioId]);
  }
}
