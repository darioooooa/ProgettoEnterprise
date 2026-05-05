export interface Viaggio {
  id?: number;
  titolo: string;
  descrizione: string;
  destinazione: string;
  prezzo: number;
  dataInizio: string;
  dataFine: string;
  tappe: any[]; // Qui puoi essere più specifico se hai un modello per le tappe
}
