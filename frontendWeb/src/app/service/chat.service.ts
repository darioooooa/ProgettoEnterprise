import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import { MessaggioChatDTO } from '../models/messaggio-chat.model';
import { AutenticazioneService } from './autenticazione.service';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = '/api/chat';
  private stompClient!: Client;

  // Canale interno ad Angular per trasmettere i messaggi in tempo reale ai componenti
  private messaggioInArrivoSource = new Subject<MessaggioChatDTO>();
  messaggioInArrivo$ = this.messaggioInArrivoSource.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AutenticazioneService
  ) {}




  ottieniOCreaStanza(viaggioId: number, viaggiatoreUsername: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stanza`, {
      params: {
        viaggioId: viaggioId.toString(),
        viaggiatoreUsername: viaggiatoreUsername
      }
    });
  }

  // Recupera lo storico dei messaggi
  ottieniCronologia(roomId: number): Observable<MessaggioChatDTO[]> {

    return this.http.get<MessaggioChatDTO[]>(`${this.apiUrl}/stanza/${roomId}/cronologia`);
  }
  ottieniStanzaPerOrganizzatore(organizzatoreUsername: string): Observable<any[]> {

    return this.http.get<any[]>(`/api/chat/organizzatore`, {
      params: { organizzatoreUsername: organizzatoreUsername }
    });
  }


  // Attiva la connessione in tempo reale con il backend usando il token di Keycloak
  connettiEIniziaAscolto(roomId: number): void {

    const token = this.authService.ottieniToken();

    this.stompClient = new Client({
      brokerURL: 'ws://localhost:4200/ws', //ws è il protocollo del WebSocket
      connectHeaders: {

        'Authorization': token ? `Bearer ${token}` : ''
      },
      debug: (str) => { console.log('STOMP: ' + str); },
      reconnectDelay: 5000, // riconnessione automatica in caso di caduta della linea
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connesso e autenticato su WebSocket con Keycloak!');

      // Ci sintonizziamo sul canale specifico della chat
      this.stompClient.subscribe(`/topic/chatroom/${roomId}`, (message: Message) => {
        if (message.body) {
          const nuovoMessaggio: MessaggioChatDTO = JSON.parse(message.body);

          this.messaggioInArrivoSource.next(nuovoMessaggio);
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Errore di sicurezza STOMP/Keycloak:', frame.headers['message']);
      const messaggioErrore = frame.headers['message'];
      if (messaggioErrore && messaggioErrore.includes('velocemente')) {
        alert(`💬 CHAT LIMIT: ${messaggioErrore}`);
      }
    };

    // Attiva il WebSocket
    this.stompClient.activate();
  }


  inviaMessaggio(roomId: number, messaggio: MessaggioChatDTO): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: `/app/chat/invia/${roomId}`,
        body: JSON.stringify(messaggio)
      });
    } else {
      console.error('Impossibile inviare il messaggio: WebSocket non connesso!');
    }
  }

  // Chiude la connessione
  disconnetti(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('WebSocket disconnesso.');
    }
  }
}
