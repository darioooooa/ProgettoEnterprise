import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../service/chat.service';
import { AutenticazioneService } from '../service/autenticazione.service';
import { MessaggioChatDTO } from '../models/messaggio-chat.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-inbox-viaggiatore',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox-viaggiatore.html',
  styleUrl: './inbox-viaggiatore.css'
})
export class InboxViaggiatoreComponent implements OnInit, OnDestroy {

  @ViewChild('scrollChat') private centroChatScroll!: ElementRef;

  listaStanze: any[] = [];
  stanzaSelezionata: any = null;
  stanzaId!: number;

  messaggiChat: MessaggioChatDTO[] = [];
  nuovoMessaggioTesto: string = '';
  mioUsername: string = '';

  messaggioAvviso: string | null = null;
  tipoAvviso: 'successo' | 'errore' = 'errore';
  isLoading: boolean = false;

  private chatSubscription!: Subscription;

  constructor(
    private chatService: ChatService,
    private auth: AutenticazioneService,
    private cdr: ChangeDetectorRef
  ) {}

  isLoggato(): boolean {
    return this.auth.isLoggato();
  }

  ngOnInit() {
    this.mioUsername = this.auth.ottieniUsername() || '';
    if (this.isLoggato()) {
      this.chatService.InizializzaWebSocketGlobale();
      this.chatService.ascoltaNotificheGlobali(this.mioUsername);


      if (this.chatSubscription) { this.chatSubscription.unsubscribe(); }

      this.chatSubscription = this.chatService.messaggioInArrivo$.subscribe(nuovoMsg => {
        // Se abbiamo una chat aperta a schermo e il messaggio è per lei
        if (this.stanzaSelezionata && nuovoMsg.chatRoomId === this.stanzaId) {
          this.messaggiChat.push(nuovoMsg);
          this.gestisciScrollChat();
          this.cdr.detectChanges();
        } else {

          const stanzaDaAggiornare = this.listaStanze.find(s => s.id === nuovoMsg.chatRoomId);
          if (stanzaDaAggiornare) {
            stanzaDaAggiornare.messaggiNonLetti = (stanzaDaAggiornare.messaggiNonLetti || 0) + 1;
            this.cdr.detectChanges();
          }
        }
      });

      this.caricaListaStanze();
    }
  }
  caricaListaStanze() {
    this.isLoading = true;
    this.chatService.ottieniStanzePerViaggiatore(this.mioUsername).subscribe({
      next: (stanze) => {

        this.listaStanze = stanze;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore recupero stanze:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
  selezionaChat(stanza: any) {
    this.stanzaSelezionata = stanza;
    this.stanzaId = stanza.id;
    this.messaggiChat = [];
    this.messaggioAvviso = null;
    this.isLoading = true;

    // Azzera il contatore della riga cliccata
    stanza.messaggiNonLetti = 0;

    this.chatService.ottieniCronologia(this.stanzaId).subscribe({
      next: (storico) => {
        this.messaggiChat = storico;
        this.gestisciScrollChat();
        this.isLoading = false;
        this.cdr.detectChanges();

        this.chatService.segnaComeLetti(this.stanzaId, this.mioUsername).subscribe({
          next: () => {
            this.chatService.ottieniNotificheTotali(this.mioUsername).subscribe(nuovoTotale => {
              this.chatService.aggiornaContatoreNotifiche(nuovoTotale);
              this.cdr.detectChanges();
            });
          },
          error: (err) => console.error("Errore nell'aggiornamento notifiche letti:", err)
        });
      },
      error: (err) => {
        console.error("Errore cronologia:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });

    // Connette il canale live per inviare i messaggi su questa stanza
    this.chatService.connettiEIniziaAscolto(this.stanzaId);
  }

  inviaMessaggioCommunity() {
    if (!this.nuovoMessaggioTesto.trim() || !this.stanzaId || this.isLoading) return;

    this.isLoading = true;

    const dto: MessaggioChatDTO = {
      chatRoomId: this.stanzaId,
      mittenteUsername: this.mioUsername,
      testo: this.nuovoMessaggioTesto.trim()
    };

    this.chatService.inviaMessaggio(this.stanzaId, dto);
    this.nuovoMessaggioTesto = '';

    setTimeout(() => {
      this.isLoading = false;
      this.cdr.detectChanges();
    }, 150);
  }

  private gestisciScrollChat() {
    try {
      setTimeout(() => {
        if (this.centroChatScroll) {
          this.centroChatScroll.nativeElement.scrollTop = this.centroChatScroll.nativeElement.scrollHeight;
          this.cdr.detectChanges();
        }
      }, 100);
    } catch (err) {}
  }

  ngOnDestroy() {
    this.chatService.disconnetti();
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }
  }
}
