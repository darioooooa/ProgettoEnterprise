import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PagamentoService {

  private apiUrl = '/api/v1/pagamento';

  constructor(private http: HttpClient) {}

  creaPaymentIntent(idPrenotazione: number): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>(`${this.apiUrl}/crea-intent/${idPrenotazione}`, {});
  }

  //Conferma e salva la ricevuta
  confermaPagamento(datiPagamento: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/conferma`, datiPagamento);
  }
}
