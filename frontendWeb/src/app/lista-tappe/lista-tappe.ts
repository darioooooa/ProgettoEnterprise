import {ChangeDetectorRef, Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {CommonModule, isPlatformBrowser} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-lista-tappe',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './lista-tappe.html',
  styleUrl: './lista-tappe.css',
})
export class ListaTappe implements OnInit{

  viaggio:any=null;
  tappe:any[]=[];
  caricamento:boolean=true;
  constructor(private route: ActivatedRoute,
              private http: HttpClient,
              private router:Router,
              private cdr: ChangeDetectorRef,
              @Inject(PLATFORM_ID) private platformId: Object
  ){}

    ngOnInit(): void {
      if (isPlatformBrowser(this.platformId)) {
        // Catturo l'ID dall'URL
        console.log("1. INIZIO CARICAMENTO PAGINA DETTAGLI");

        // Controlliamo che Angular legga bene l'ID dall'URL (es. l'8)
        const idStringa = this.route.snapshot.paramMap.get('id');
        console.log("2. ID TROVATO NELL'URL:", idStringa);

        if (idStringa) {
          const viaggioId = Number(idStringa);
          console.log("3. FACCIO LA CHIAMATA HTTP PER IL VIAGGIO ID:", viaggioId);

          this.http.get<any>(`http://localhost:8080/api/v1/viaggi/${viaggioId}`).subscribe({
            next: (dati) => {
              console.log("4. DATI RICEVUTI CON SUCCESSO DAL SERVER!", dati);

              this.viaggio = dati;
              this.tappe = dati.tappe || [];
              this.caricamento = false; // Spegniamo il loader

              this.cdr.detectChanges();

              console.log("5. CARICAMENTO SPENTO. LE TAPPE DA MOSTRARE SONO:", this.tappe);
            },
            error: (err) => {
              console.error("ERRORE DURANTE LA CHIAMATA HTTP:", err);
              this.caricamento = false;
              this.cdr.detectChanges();
            }
          });
        } else {
          console.error("NESSUN ID TROVATO NELL'URL!");
          this.caricamento = false;
        }
      }
    }

  tornaIndietro() {
    this.router.navigate(['/organizzatore']);
  }
}
