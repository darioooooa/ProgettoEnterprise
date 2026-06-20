import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
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

  // Centralino (BehaviorSubject) per notificare la Navbar superiore in tempo reale
  private notificheTotaliSource = new BehaviorSubject<number>(0);
  notificheTotali$ = this.notificheTotaliSource.asObservable();

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

  /**
   * Recupera lo storico di tutti i messaggi di una specifica chat room
   */
  ottieniCronologia(roomId: number): Observable<MessaggioChatDTO[]> {
    return this.http.get<MessaggioChatDTO[]>(`${`${this.apiUrl}/stanza/${roomId}`}/cronologia`);
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


  connettiEIniziaAscolto(roomId: number): void {
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

    this.stompClient.onConnect = (frame) => {
      console.log('Connesso e autenticato su WebSocket con Keycloak!');

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

    this.stompClient.activate();
  }

  /**
   * Ascolta il canale di notifiche globale per l'utente loggato.
   * Aggiorna il contatore della Navbar in tempo reale senza fare chiamate HTTP di polling.
   */
  ascoltaNotificheGlobali(mioUsername: string): void {
    // Controllo difensivo: se il client STOMP non è ancora pronto o connesso,
    // rimanda l'ascolto di un secondo per evitare eccezioni di blocco
    if (!this.stompClient || !this.stompClient.connected) {
      setTimeout(() => this.ascoltaNotificheGlobali(mioUsername), 1000);
      return;
    }

    // Ci iscriviamo al topic privato delle notifiche dell'utente loggato
    this.stompClient.subscribe(`/topic/notifiche/${mioUsername}`, (message: Message) => {
      if (message.body) {
        console.log("🔔 [WEBSOCKET] Nuova notifica ricevuta sul canale globale!");

        // Estraiamo il valore corrente del BehaviorSubject, incrementiamo e notifichiamo
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
    const canaleAmicizie = `/topic/notifiche/${username}`;
    this.stompClient.subscribe(canaleAmicizie, (messaggio: any) => {
      console.log("Nuova richiesta di amicizia ricevuta in tempo reale!");
      this.notificheAmicizia$.next(); // Suona il megafono!
    });
  }

  disconnetti(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('WebSocket disconnesso.');
    }
  }


}
