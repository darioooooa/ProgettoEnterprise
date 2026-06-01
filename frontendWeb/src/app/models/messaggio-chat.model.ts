export interface MessaggioChatDTO {
  id?: number;
  chatRoomId: number;
  mittenteUsername: string;
  testo: string;
  dataInvio?: string | Date;
}
