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
  maxPartecipanti: number;
  partecipantiAttuali: number;
  cittaPartenza: string;
  stato:string;

  mediaRecensioni?: number;
  numeroRecensioni?: number;
  organizzatoreId?: number;
  organizzatoreUsername?: string;
}
