export interface Prenotazione {
  id: number;
  dataPrenotazione: string;
  numeroPersone: number;
  viaggiatoreId: number;
  viaggiatoreUsername: string;
  viaggioId: number;
  viaggioTitolo: string;
  viaggioDataInizio?: string;
  viaggioDataFine?: string;
  stato: string;
}
