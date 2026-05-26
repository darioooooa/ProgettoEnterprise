import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink,Router} from '@angular/router';
import {ViaggioService} from '../service/viaggio.service';
import {AutenticazioneService} from '../service/autenticazione.service';
import {MapComponent} from '../map/map';

@Component({
  selector: 'app-schermata-organizzatore',
  standalone: true,
  imports: [CommonModule, RouterLink, MapComponent],
  templateUrl: './schermataOrganizzatore.html',
  styleUrl: './schermataOrganizzatore.css'
})
export class SchermataOrganizzatoreComponent implements OnInit {

  listaDeiViaggi: any[] = [];
  paginaCorrente: number = 0;
  totalePagine: number = 0;

  constructor(
    private viaggioService: ViaggioService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.caricaViaggi();
  }

  caricaViaggi(pagina: number = 0) {
    this.paginaCorrente = pagina;
    this.viaggioService.getViaggi(this.paginaCorrente).subscribe({
      next: (data) => {
        this.listaDeiViaggi = data.content;
        this.totalePagine = data.totalPages;
        this.cdr.detectChanges();
        console.log('Viaggi caricati:', data);
      },
      error: (e) => console.error('Errore durante il caricamento:', e)
    });
  }

  paginaSuccessiva() {
    if (this.paginaCorrente < this.totalePagine - 1) {
      this.caricaViaggi(this.paginaCorrente + 1);
    }
  }

  paginaPrecedente() {
    if (this.paginaCorrente > 0) {
      this.caricaViaggi(this.paginaCorrente - 1);
    }
  }

  modifica(viaggio: any) {
    console.log("Voglio modificare il viaggio:", viaggio);

    alert("Funzione modifica per: " + viaggio.destinazione);
  }

  elimina(viaggio: any) {
    if (confirm("Sei sicuro di voler eliminare " + viaggio.destinazione + "?")) {
      console.log("Elimino il viaggio con ID:", viaggio.id);


      this.listaDeiViaggi = this.listaDeiViaggi.filter(v => v !== viaggio);
    }
  }

  apriModalCreazione() {
    alert("Qui si aprirebbe il form per il nuovo viaggio!");
  }

  vaiAiDettagli(viaggioId: number) {
    console.log("Navigo verso la pagina dettagli del viaggio ID:", viaggioId);
    this.router.navigate(['/lista-tappe', viaggioId]);
  }
}
