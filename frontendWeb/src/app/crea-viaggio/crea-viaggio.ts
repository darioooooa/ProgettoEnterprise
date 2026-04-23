import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import { FormsModule } from '@angular/forms';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-crea-viaggio',
  standalone: true,
  imports: [CommonModule,
  FormsModule,
  RouterLink],
  templateUrl: './crea-viaggio.html',
  styleUrl: './crea-viaggio.css',
})
export class CreaViaggio {
  titolo: string = "Avventura nella Savana";
  descrizione: string = "Un viaggio indimenticabile nel cuore della Savana africana";
  destinazione: string=  "Kenya" ;
  prezzo: number= 1250.00;
  dataInizio: string = "2026-08-10";
  dataFine: string = "2026-08-20";
  tappe: any[]= [
        {
        titolo: "Arrivo al parco Masai Mara",
        descrizione: "Relax nelle acque termali dopo il volo."
        }
      ];
}
