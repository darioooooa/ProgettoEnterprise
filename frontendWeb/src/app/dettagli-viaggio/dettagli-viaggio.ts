import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AutenticazioneService } from '../service/autenticazione.service';
import { Viaggio } from '../models/viaggio.model';

@Component({
  selector: 'app-dettagli-viaggio',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dettagli-viaggio.html',
  styleUrl: './dettagli-viaggio.css'
})
export class DettagliViaggio implements OnInit {


  viaggioDati: Viaggio|null = null ;
  erroreMessaggio: string = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AutenticazioneService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    // Eseguiamo la logica solo all'interno del browser (no Server-Side Rendering iniziale)
    if (isPlatformBrowser(this.platformId)) {

      // Catturiamo l'id del viaggio dall'URL
      const idViaggio = this.route.snapshot.paramMap.get('id');

      if (idViaggio) {
        // Applichiamo il delay di 500ms per dare tempo a Keycloak di stabilizzare la sessione
        setTimeout(() => {
          this.recuperaDettagliViaggioDalDB(idViaggio);
        });
      }
    }
  }

  recuperaDettagliViaggioDalDB(id: string): void {
    const urlBackend = `http://localhost:8080/api/v1/viaggi/${id}`;

    // Estraiamo il token e lo inseriamo negli Header della richiesta
    const token = this.authService.ottieniToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    console.log(`Chiamata GET in corso su: ${urlBackend}`);

    // Specifichiamo che la GET risponde esattamente con il tipo <Viaggio>
    this.http.get<Viaggio>(urlBackend, { headers }).subscribe({
      next: (viaggioDTO: Viaggio) => {
        this.viaggioDati = viaggioDTO; // Usa viaggioDati
        console.log('Dati del viaggio caricati con successo:', this.viaggioDati);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel recupero del viaggio:', err);
        this.erroreMessaggio = 'Impossibile caricare i dettagli di questo viaggio. Riprova più tardi.';
        this.cdr.detectChanges();
      }
    });
  }
}
