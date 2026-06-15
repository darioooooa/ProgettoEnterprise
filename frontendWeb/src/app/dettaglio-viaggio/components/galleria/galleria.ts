import { Component, OnInit, Input, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViaggioService } from '../../../service/viaggio.service';

@Component({
  selector: 'app-galleria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './galleria.html',
  styleUrl: './galleria.css'
})
export class GalleriaComponent implements OnInit {
  @Input() viaggioId!: number;
  @Input() isMioViaggio: boolean = false;

  immagini: any[] = [];
  immagineCorrenteIndex = 0;
  nuovaImmagineUrl: string = '';
  nuovaImmaginePubblica: boolean = true;
  idImmagineDaEliminare: number | null = null;

  messaggioAvviso: string | null = null;
  tipoAvviso: 'successo' | 'errore' = 'errore';

  isLoading: boolean = false;

  constructor(private viaggioService: ViaggioService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.caricaGalleria();
  }

  caricaGalleria() {
    this.viaggioService.getGalleria(this.viaggioId).subscribe({
      next: (res) => {
        this.immagini = res || [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore nel caricamento della galleria:", err)
    });
  }

  aggiungiImmagineGalleria() {
    if (this.isLoading) return;

    if (!this.nuovaImmagineUrl || !this.nuovaImmagineUrl.trim()){
      this.tipoAvviso = 'errore';
      this.messaggioAvviso = "Inserire un URL valido per l'immagine.";
      this.cdr.detectChanges();
      return;
    }
    this.messaggioAvviso = null;
    this.isLoading = true;

    this.viaggioService.aggiungiImmagine(this.viaggioId, this.nuovaImmagineUrl, this.nuovaImmaginePubblica).subscribe({
      next: () => {
        this.nuovaImmagineUrl = '';
        this.nuovaImmaginePubblica = true;
        this.viaggioService.getGalleria(this.viaggioId).subscribe({
          next: (res) => {
            this.immagini = res || [];
            this.tipoAvviso = 'successo';
            this.messaggioAvviso = "Immagine aggiunta alla galleria!";
            if (this.immagini.length > 0) {
              this.immagineCorrenteIndex = this.immagini.length - 1;
            }
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = err.error?.messaggio || "Impossibile caricare l'immagine. Verifica il link.";
        this.isLoading = false;
        this.scattaScrollAvviso();
      }
    });
  }

  cambiaVisibilitaImmagine(img: any) {
    if (!img || !this.isMioViaggio || this.isLoading) return;

    const nuovaVisibilita = !img.pubblica;
    this.isLoading = true;

    this.viaggioService.modificaVisibilita(this.viaggioId, img.id, nuovaVisibilita).subscribe({
      next: (res: any) => {
        img.pubblica = res.pubblica;
        this.tipoAvviso = 'successo';
        this.messaggioAvviso = `Visibilità aggiornata: l'immagine è ora ${res.pubblica ? 'pubblica' : 'privata'}.`;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = err.error?.messaggio || "Impossibile modificare la visibilità.";
        this.isLoading = false;
        this.scattaScrollAvviso();
      }
    });
  }

  cancellaImmagineGalleria(idImmagine: number) {
    if (!idImmagine || this.isLoading) return;

    if (this.idImmagineDaEliminare !== idImmagine) {
      this.idImmagineDaEliminare = idImmagine;
      setTimeout(() => {
        if (this.idImmagineDaEliminare === idImmagine) {
          this.idImmagineDaEliminare = null;
          this.cdr.detectChanges();
        }
      }, 4000);
      return;
    }
    this.idImmagineDaEliminare = null;
    this.messaggioAvviso = null;
    this.isLoading = true;

    this.viaggioService.eliminaImmagine(this.viaggioId, idImmagine).subscribe({
      next: () => {
        this.viaggioService.getGalleria(this.viaggioId).subscribe({
          next: (res) => {
            this.immagini = res || [];
            this.tipoAvviso = 'successo';
            this.messaggioAvviso = "Immagine rimossa dal viaggio.";
            if (this.immagineCorrenteIndex >= this.immagini.length) {
              this.immagineCorrenteIndex = Math.max(0, this.immagini.length - 1);
            }
            this.isLoading = false;
            this.scattaScrollAvviso();
          },
          error: (err) => {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = err.error?.messaggio || "Impossibile eliminare l'immagine.";
        this.isLoading = false;
        this.scattaScrollAvviso();
      }
    });
  }

  navigaLightbox(direzione: number, event: Event) {
    event.stopPropagation();
    if (this.immagini.length === 0 || this.isLoading) return; // Impedisce la navigazione se la galleria è in aggiornamento
    this.immagineCorrenteIndex += direzione;
    if (this.immagineCorrenteIndex >= this.immagini.length) this.immagineCorrenteIndex = 0;
    else if (this.immagineCorrenteIndex < 0) this.immagineCorrenteIndex = this.immagini.length - 1;
    this.cdr.detectChanges();
  }

  private scattaScrollAvviso() {
    this.cdr.detectChanges();
    setTimeout(() => {
      const elementoBanner = document.getElementById('avviso-galleria');
      if (elementoBanner) {
        elementoBanner.scrollIntoView({
          behavior: 'smooth',
          block: 'center'
        });
      }
    }, 50);
  }
}
