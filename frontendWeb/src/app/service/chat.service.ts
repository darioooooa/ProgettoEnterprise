import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import { MessaggioChatDTO } from '../models/messaggio-chat.model';
import { AutenticazioneService } from './autenticazione.service';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = '/api/chat';
  private stompClient!: Client;


  private messaggioInArrivoSource = new Subject<MessaggioChatDTO>();
  messaggioInArrivo$ = this.messaggioInArrivoSource.asObservable();


  private notificheTotaliSource = new BehaviorSubject<number>(0);
  notificheTotali$ = this.notificheTotaliSource.asObservable();

  // 🟢 Mantiene traccia della sottoscrizione attiva alla stanza singola per poterla rimuovere in modo mirato
  private stanzaSubscription: StompSubscription | null = null;

  constructor(
    private http: HttpClient,
    private authService: AutenticazioneService
  ) {}

  aggiornaContatoreNotifiche(nuovoConteggio: number): void {
    this.notificheTotaliSource.next(nuovoConteggio);
  }

  ottieniOCreaStanza(viaggioId: number, viaggiatoreUsername: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stanza`, {
      params: {
        viaggioId: viaggioId.toString(),
        viaggiatoreUsername: viaggiatoreUsername
      }
    });
  }


  ottieniCronologia(roomId: number): Observable<MessaggioChatDTO[]> {
    return this.http.get<MessaggioChatDTO[]>(`${this.apiUrl}/stanza/${roomId}/cronologia`);
  }

  ottieniStanzaPerOrganizzatore(organizzatoreUsername: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/organizzatore`, {
      params: { organizzatoreUsername: organizzatoreUsername }
    });
  }

  ottieniNotificheTotali(username: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/notifiche-totali`, {
      params: { username: username }
    });
  }


  segnaComeLetti(roomId: number, username: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${roomId}/leggi`, {}, {
      params: { username: username }
    });
  }


  InizializzaWebSocketGlobale(): void {
    if (this.stompClient && this.stompClient.connected) return;

    const token = this.authService.ottieniToken();

    this.stompClient = new Client({
      brokerURL: 'ws://localhost:4200/ws',
      connectHeaders: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      debug: (str) => { console.log('STOMP: ' + str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.stompClient.onStompError = (frame) => {
      console.error('Errore di sicurezza STOMP/Keycloak:', frame.headers['message']);
    };

    this.stompClient.activate();
  }

  connettiEIniziaAscolto(roomId: number): void {

    this.InizializzaWebSocketGlobale();

    const effettuaSottoscrizioneStanza = () => {
      // Rimuoviamo una sottoscrizione precedente se attiva per evitare duplicati
      if (this.stanzaSubscription) {
        this.stanzaSubscription.unsubscribe();
      }

      this.stanzaSubscription = this.stompClient.subscribe(`/topic/chatroom/${roomId}`, (message: Message) => {
        if (message.body) {
          const nuovoMessaggio: MessaggioChatDTO = JSON.parse(message.body);
          this.messaggioInArrivoSource.next(nuovoMessaggio);
        }
      });
      console.log(`Sottoscritto con successo ai messaggi della stanza: ${roomId}`);
    };


    if (this.stompClient.connected) {
      effettuaSottoscrizioneStanza();
    } else {
      this.stompClient.onConnect = () => {
        console.log('Connesso su WebSocket!');
        effettuaSottoscrizioneStanza();
      };
    }
  }


  ascoltaNotificheGlobali(mioUsername: string): void {
    this.InizializzaWebSocketGlobale();

    if (!this.stompClient.connected) {
      setTimeout(() => this.ascoltaNotificheGlobali(mioUsername), 1000);
      return;
    }

    this.stompClient.subscribe(`/topic/notifiche/${mioUsername}`, (message: Message) => {
      if (message.body) {
        console.log("🔔 [WEBSOCKET] Nuova notifica ricevuta sul canale globale!");
        const contatoreAttuale = this.notificheTotaliSource.value;
        this.aggiornaContatoreNotifiche(contatoreAttuale + 1);
      }
    });
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

  ottieniStanzePerViaggiatore(viaggiatoreUsername: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/viaggiatore`, {
      params: { viaggiatoreUsername: viaggiatoreUsername }
    });
  }

  public notificheAmicizia$ = new Subject<void>();
  ascoltaNotificheAmicizia(username: string) {
    if (!this.stompClient || !this.stompClient.connected) {
      setTimeout(() => this.ascoltaNotificheAmicizia(username), 1000);
      return;
    }
    this.InizializzaWebSocketGlobale();

    if (!this.stompClient.connected) {
      setTimeout(() => this.ascoltaNotificheAmicizia(username), 1000);
      return;
    }

    const canaleAmicizie = `/topic/notifiche/${username}`;
    this.stompClient.subscribe(canaleAmicizie, (messaggio: any) => {
      console.log("Nuova richiesta di amicizia ricevuta in tempo reale!");
      this.notificheAmicizia$.next();
    });
  }


  disconnetti(): void {
    if (this.stanzaSubscription) {
      this.stanzaSubscription.unsubscribe();
      this.stanzaSubscription = null;
      console.log('Disiscritto dalla stanza singola. Il WebSocket globale rimane ACCESO per le notifiche della Navbar.');
    }
  }
}
