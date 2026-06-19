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

  // Gestione avvisi del tuo file originale
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
        this.tipoAvviso = 'errore';
        this.messaggioAvviso = "Impossibile caricare l'elenco delle chat.";
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

    this.chatService.ottieniCronologia(this.stanzaId).subscribe({
      next: (storico) => {
        this.messaggiChat = storico;
        this.gestisciScrollChat();
        this.isLoading = false;
        this.cdr.detectChanges();


        this.chatService.segnaComeLetti(this.stanzaId, this.mioUsername).subscribe({
          next: () => {
            // Dopo aver letto i messaggi, chiediamo al servizio di ricalcolare il totale aggiornato
            this.chatService.ottieniNotificheTotali(this.mioUsername).subscribe(nuovoTotale => {
              this.chatService.aggiornaContatoreNotifiche(nuovoTotale);
              this.cdr.detectChanges(); // Forza la Navbar a nascondere o diminuire il badge
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

    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }

    this.chatService.connettiEIniziaAscolto(this.stanzaId);

    this.chatSubscription = this.chatService.messaggioInArrivo$.subscribe(nuovoMsg => {
      if (nuovoMsg.chatRoomId === this.stanzaId) {
        this.messaggiChat.push(nuovoMsg);
        this.gestisciScrollChat();
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
