import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ViaggioService} from '../service/viaggio.service';
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
  isLoading: boolean = false;


  constructor(private rotta:ActivatedRoute,
              private router:Router,
              private location: Location,
              private viaggioService: ViaggioService,
              private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.viaggioService.viaggiSelezionati$.subscribe(viaggiInMemoria => {

      if (viaggiInMemoria && viaggiInMemoria.length > 0) {

        this.viaggi = [...viaggiInMemoria];
        this.cdr.detectChanges();
        console.log("🚀 Titoli mostrati all'istante! Arricchisco i dettagli in background...");


        this.viaggi.forEach((viaggioMappa, index) => {
          const id = viaggioMappa.id || viaggioMappa.idViaggio;

          if (id) {
            this.viaggioService.getViaggioById(Number(id)).subscribe({
              next: (viaggioCompleto: any) => {
                // Sostituiamo l'oggetto leggero con quello completo del DB con descrizione, prezzo, ecc.
                this.viaggi[index] = viaggioCompleto;

                this.cdr.detectChanges();
              },
              error: (err) => console.error("Errore nel recupero dettagli per ID: " + id, err)
            });
          }
        });

      } else {
        //in caso non funziona va sempre il metodo degli id
        this.rotta.queryParams.subscribe(params => {
          if (params['ids']) {
            const listaIds = params['ids'].split(',');
            this.caricaViaggi(listaIds);
          }
        });
      }
    });
  }

  private caricaViaggi(listaIds: any) {
    if (listaIds.length === 0) return;

    this.isLoading = true;
    let chiamateCompletate = 0;

    // Facciamo un ciclo e stampiamo le card appena arrivano
    listaIds.forEach((id: any) => {
      this.viaggioService.getViaggioById(Number(id)).subscribe({
        next: (viaggio: any) => {
          this.viaggi.push(viaggio);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore nel recupero del viaggio ID " + id, err);
        },
        complete: () => {
          chiamateCompletate++;
          if (chiamateCompletate === listaIds.length) {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        }
      });
    });
  }

  tornaIndietro() {
    if (this.isLoading) return;
    this.location.back();
  }

  vaiAiDettagli(viaggioId: number) {
    if (this.isLoading) return;

    console.log("Navigo verso i dettagli del viaggio:", viaggioId);
    this.router.navigate(['/viaggi', viaggioId]);
  }
}
