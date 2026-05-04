import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-schermata-home',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './schermataOrganizzatore.html',
  styleUrl: './schermataOrganizzatore.css'
})
export class SchermataOrganizzatoreComponent {
  listaDeiViaggi: any[] = [];
  ngOnInit(): void {
    this.caricaViaggi();
  }

  caricaViaggi() {
    // Esempio manuale per testare se il rosso sparisce:
    this.listaDeiViaggi = [
      { destinazione: 'Bali', prezzo: 1200 },
      { destinazione: 'Sahara', prezzo: 850 }
    ];
  }
  modifica(viaggio: any) {
    console.log("Voglio modificare il viaggio:", viaggio);

    alert("Funzione modifica per: " + viaggio.destinazione);
  }

  // Funzione per l'eliminazione
  elimina(viaggio: any) {
    if(confirm("Sei sicuro di voler eliminare " + viaggio.destinazione + "?")) {
      console.log("Elimino il viaggio con ID:", viaggio.id);


      this.listaDeiViaggi = this.listaDeiViaggi.filter(v => v !== viaggio);
    }
  }
  apriModalCreazione() {
    alert("Qui si aprirebbe il form per il nuovo viaggio!");
  }

}
