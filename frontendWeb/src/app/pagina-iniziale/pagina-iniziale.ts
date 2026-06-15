import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagina-iniziale',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './pagina-iniziale.html',
  styleUrl: './pagina-iniziale.css'
})
export class PaginaInizialeComponent implements OnInit {

  isLoading: boolean = false;

  constructor(
    private auth: AutenticazioneService,
    private navigatore: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.auth.isLoggato()) {
      this.isLoading = true;
      this.navigatore.navigate(['/home']).then(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      });
    }
  }
}
