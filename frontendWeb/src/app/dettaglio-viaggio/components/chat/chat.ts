import { Component, OnInit, OnDestroy, Input, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../../service/chat.service';
import { AutenticazioneService } from '../../../service/autenticazione.service';
import { MessaggioChatDTO } from '../../../models/messaggio-chat.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  @Input() viaggioId!: number;
  @Input() mioUsername!: string;

  @ViewChild('scrollChat') private centroChatScroll!: ElementRef;

  messaggioAvviso: string | null = null;
  tipoAvviso: 'successo' | 'errore' = 'errore';

  stanzaId!: number;
  nuovoMessaggioTesto: string = '';
  messaggiChat: MessaggioChatDTO[] = [];
  private chatSubscription!: Subscription;

  isLoading: boolean = false;

  constructor(
    private chatService: ChatService,
    private auth: AutenticazioneService,
    private cdr: ChangeDetectorRef
  ) {}

  isLoggato(): boolean {
    return this.auth.isLoggato();
  }

  ngOnInit() {
    if (this.viaggioId && this.mioUsername) {
      this.inizializzaChatCommunity();
    }
  }

  inizializzaChatCommunity() {
    this.isLoading = true;
    this.chatService.ottieniOCreaStanza(this.viaggioId, this.mioUsername).subscribe({
      next: (idRitorno) => {
        this.stanzaId = idRitorno;

        this.chatService.ottieniCronologia(this.stanzaId).subscribe({
          next: (storico) => {
            this.messaggiChat = storico;
            this.gestisciScrollChat();
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error("Errore cronologia:", err);
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        });

        this.chatService.connettiEIniziaAscolto(this.stanzaId);

        this.chatSubscription = this.chatService.messaggioInArrivo$.subscribe(nuovoMsg => {
          if (nuovoMsg.chatRoomId === this.stanzaId) {
            this.messaggiChat.push(nuovoMsg);
            this.gestisciScrollChat();
          }
        });
      },
      error: (err) => {
        console.error("Impossibile avviare la chat:", err);
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = "Impossibile caricare la chat. Riprova più tardi.";
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
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
