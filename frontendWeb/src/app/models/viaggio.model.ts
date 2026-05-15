export interface Viaggio {
  id?: number;
  titolo: string;
  descrizione: string;
  destinazione: string;
  prezzo: number;
  dataInizio: string;
  dataFine: string;
  tappe: any[];
  longitudine: number;
  latitudine: number;
}
