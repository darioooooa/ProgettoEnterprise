import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import { ViaggioService } from '../service/viaggio.service';
import { UtenteService } from '../service/utente.service';

@Component({
  selector: 'app-schermata-utente',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schermata-utente.html',
  styleUrl: './schermata-utente.css'
})
export class SchermataUtente implements OnInit {
  isMioProfilo: boolean = true;

  isLoading: boolean = false;

  nome: string | null = '';
  cognome: string | null = '';
  username: string | null = '';
  email: string | null = '';
  ruolo: string | null = '';

  viaggiOrganizzati: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private authService: AutenticazioneService,
    private utenteService: UtenteService,
    private viaggioService: ViaggioService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.route.paramMap.subscribe(params => {
        const idDaUrl = params.get('id');
        this.viaggiOrganizzati = []; // Reset
        this.isLoading = true;
        this.cdr.detectChanges();

        if (!idDaUrl) {
          // Profilo personale
          this.isMioProfilo = true;
          this.nome = this.authService.ottieniNome();
          this.cognome = this.authService.ottieniCognome();
          this.username = this.authService.ottieniUsername();
          this.email = this.authService.ottieniEmail();
          this.ruolo = this.authService.ottieniRuolo();

          if (this.ruolo === 'ROLE_ORGANIZZATORE') {
            const mioIdDallAuth = this.authService.ottieniId();
            if (mioIdDallAuth) {
              this.viaggioService.getViaggiByOrganizzatore(Number(mioIdDallAuth)).subscribe({
                next: (viaggi) => {
                  this.viaggiOrganizzati = viaggi;
                  this.isLoading = false;
                  this.cdr.detectChanges();
                },
                error: (err) => {
                  console.error("Errore nei tuoi viaggi:", err);
                  this.isLoading = false;
                  this.cdr.detectChanges();
                }
              });
            } else {
              this.isLoading = false;
              this.cdr.detectChanges();
            }
          } else {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        } else {
          // Profilo pubblico dell'utente selezionato
          this.isMioProfilo = false;
          this.caricaProfiloPubblico(Number(idDaUrl));
        }
      });
    }
  }

  caricaProfiloPubblico(id: number) {
    this.utenteService.getProfiloById(id).subscribe({
      next: (databaseUser) => {
        this.nome = databaseUser.nome;
        this.cognome = databaseUser.cognome;
        this.username = databaseUser.username;
        this.email = databaseUser.email;
        this.ruolo = databaseUser.ruolo;

        if (this.ruolo === 'ROLE_ORGANIZZATORE') {
          this.viaggioService.getViaggiByOrganizzatore(id).subscribe({
            next: (viaggi) => {
              this.viaggiOrganizzati = viaggi;
              this.isLoading = false;
              this.cdr.detectChanges();
            },
            error: (err) => {
              console.error("Errore viaggi pubblico:", err);
              this.isLoading = false;
              this.cdr.detectChanges();
            }
          });
        } else {
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error("Errore caricamento profilo tramite ID:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  vaiAlDettaglioViaggio(viaggioId: number) {
    if (this.isLoading) return;
    this.isLoading = true;
    this.router.navigate(['/viaggi', viaggioId]);
  }

  isFuturo(dataInizioStr: string | null): boolean {
    if (!dataInizioStr) return false;

    const dataViaggio = new Date(dataInizioStr);
    const oggi = new Date();

    oggi.setHours(0, 0, 0, 0);
    dataViaggio.setHours(0, 0, 0, 0);

    return dataViaggio > oggi;
  }
}
