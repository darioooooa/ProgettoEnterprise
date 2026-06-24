import {ChangeDetectorRef, Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ItinerarioService} from '../service/itinerario.service';

@Component({
  selector: 'app-richieste-condivisione-itinerari',
  standalone:true,
  imports: [CommonModule],
  templateUrl: './richieste-condivisione-itinerari.html',
  styleUrl: './richieste-condivisione-itinerari.css',
})
export class RichiesteCondivisioneItinerari {
  public isLoading: boolean = false;
  public inviti: any[] = [];
  constructor(private itinerarioService: ItinerarioService,
              private cdr:ChangeDetectorRef) {}

  ngOnInit() {
    this.caricaInviti();
  }

  caricaInviti() {
    this.isLoading = true;
    this.itinerarioService.getInvitiInSospeso().subscribe({
      next: (data) => {
          this.inviti = data ? data : [];
          this.isLoading = false;

          this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore recupero inviti:", err);
          this.isLoading = false;
          this.cdr.detectChanges();
      }
    });
  }

  accettaInvito(idItinerario: number) {
    this.isLoading = true;
    this.itinerarioService.accettaInvito(idItinerario).subscribe({
      next: () => {
        this.inviti = [...this.inviti.filter(i => i.idItinerario !== idItinerario)];
        this.isLoading = false;

        // per togliere il numerino delle notifiche una volta accettato
        this.itinerarioService.aggiornaNotifiche.next();
        this.cdr.detectChanges();
        alert("Invito accettato! Troverai l'itinerario nei tuoi preferiti.");
      },
      error: (err) => {
        console.error("Errore durante l'accettazione:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
        alert("Errore durante l'accettazione dell'invito.");
      }
    });
  }

  rifiutaInvito(idItinerario: number) {
    if (confirm("Sei sicuro di voler rifiutare questo invito?")) {
      this.isLoading = true;
      this.itinerarioService.rifiutaInvito(idItinerario).subscribe({
        next: () => {
          this.inviti = [...this.inviti.filter(i => i.idItinerario !== idItinerario)];
          this.isLoading = false;

          this.itinerarioService.aggiornaNotifiche.next();

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("Errore durante il rifiuto", err);
          this.isLoading = false;
          this.cdr.detectChanges();
          alert("Errore durante il rifiuto dell'invito.");
        }
      });
    }
  }
}
