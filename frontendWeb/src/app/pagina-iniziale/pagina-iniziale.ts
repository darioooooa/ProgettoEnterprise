import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticazioneService } from '../service/autenticazione.service';

@Component({
  selector: 'app-pagina-iniziale',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './pagina-iniziale.html',
  styleUrl: './pagina-iniziale.css'
})
export class PaginaInizialeComponent implements OnInit {

  constructor(
    private auth: AutenticazioneService,
    private navigatore: Router
  ) {}

  ngOnInit() {
    if (this.auth.isLoggato()) {
      this.navigatore.navigate(['/home']);
    }
  }
}
