import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../service/chat.service';
import { AutenticazioneService } from '../service/autenticazione.service';
import { MessaggioChatDTO } from '../models/messaggio-chat.model';
import { Subscription } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-inbox-organizzatore',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox-organizzatore.html',
  styleUrl: './inbox-organizzatore.css'
})
export class InboxOrganizzatore implements OnInit, OnDestroy {

  @ViewChild('scrollInboxChat') private chatScrollContainer!: ElementRef;

  stanze: any[] = [];
  stanzaSelezionata: any = null;

  mioUsername!: string;
  nuovoMessaggioTesto: string = '';
  messaggiChat: MessaggioChatDTO[] = [];

  private chatSubscription!: Subscription;

  constructor(
    private chatService: ChatService,
    private authService: AutenticazioneService,
    private cdr: ChangeDetectorRef
    // Rimosso RouterLink da qui, non serve!
  ) {}

  ngOnInit(): void {
    this.mioUsername = this.authService.ottieniUsername() || '';
    this.caricaListaStanze();
  }

  caricaListaStanze(): void {
    this.chatService.ottieniStanzaPerOrganizzatore(this.mioUsername).subscribe({
      next: (lista) => {
        this.stanze = lista;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore nel recupero della inbox:", err)
    });
  }

  selezionaChat(stanza: any): void {
    this.disconnettiChatCorrente();

    this.stanzaSelezionata = stanza;
    this.messaggiChat = [];

    this.chatService.ottieniCronologia(stanza.id).subscribe({
      next: (storico) => {
        this.messaggiChat = storico;
        this.autoscroll();
      }
    });

    this.chatService.connettiEIniziaAscolto(stanza.id);

    this.chatSubscription = this.chatService.messaggioInArrivo$.subscribe(nuovoMsg => {
      if (nuovoMsg.chatRoomId === this.stanzaSelezionata.id) {
        this.messaggiChat.push(nuovoMsg);
        this.autoscroll();
      }
    });
  }

  inviaMessaggio(): void {
    if (!this.nuovoMessaggioTesto.trim() || !this.stanzaSelezionata) return;

    const dto: MessaggioChatDTO = {
      chatRoomId: this.stanzaSelezionata.id,
      mittenteUsername: this.mioUsername,
      testo: this.nuovoMessaggioTesto.trim()
    };

    this.chatService.inviaMessaggio(this.stanzaSelezionata.id, dto);
    this.nuovoMessaggioTesto = '';
  }

  private autoscroll(): void {
    try {
      setTimeout(() => {
        if (this.chatScrollContainer) {
          this.chatScrollContainer.nativeElement.scrollTop = this.chatScrollContainer.nativeElement.scrollHeight;
          this.cdr.detectChanges();
        }
      }, 50);
    } catch (err) {}
  }

  private disconnettiChatCorrente(): void {
    this.chatService.disconnetti();
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }
  }

  ngOnDestroy(): void {
    this.disconnettiChatCorrente();
  }
}
