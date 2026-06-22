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
        const stanzaTarget = this.listaStanze.find(s => s.id === nuovoMsg.chatRoomId);
        if (stanzaTarget) {
          stanzaTarget.dataUltimoMessaggio = nuovoMsg.dataInvio;
        }

        if (this.stanzaSelezionata && nuovoMsg.chatRoomId === this.stanzaId) {
          this.messaggiChat.push(nuovoMsg);
          this.gestisciScrollChat();
          this.cdr.detectChanges();
        } else {
          if (stanzaTarget) {
            stanzaTarget.messaggiNonLetti = (stanzaTarget.messaggiNonLetti || 0) + 1;
            this.cdr.detectChanges();
          }
        }

        this.listaStanze = [...this.listaStanze];
        this.cdr.detectChanges();
      });

      this.caricaListaStanze();
    }
  }

  caricaListaStanze() {
    this.isLoading = true;
    this.chatService.ottieniStanzePerViaggiatore(this.mioUsername).subscribe({
      next: (stanze) => {
        this.listaStanze = stanze || [];
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

  get stanzeOrdinate(): any[] {
    return [...this.listaStanze].sort((a, b) => {
      const parsingData = (data: any): number => {
        if (!data) return 0;
        if (Array.isArray(data)) {
          return new Date(data[0], data[1] - 1, data[2], data[3], data[4], data[5] || 0).getTime();
        }
        const t = new Date(data).getTime();
        return isNaN(t) ? 0 : t;
      };
      const tempoA = parsingData(a.dataUltimoMessaggio);
      const tempoB = parsingData(b.dataUltimoMessaggio);
      if (tempoA === 0 && tempoB === 0) return b.id - a.id;
      return tempoB - tempoA;
    });
  }

  selezionaChat(stanza: any) {
    this.stanzaSelezionata = stanza;
    this.stanzaId = stanza.id;
    this.messaggiChat = [];
    this.messaggioAvviso = null;
    this.isLoading = true;

    stanza.messaggiNonLetti = 0;

    this.chatService.ottieniCronologia(this.stanzaId).subscribe({
      next: (storico) => {
        this.messaggiChat = storico || [];
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

    // Sposta in cima all'invio
    if (this.stanzaSelezionata) {
      this.stanzaSelezionata.dataUltimoMessaggio = new Date().toISOString();
    }
    this.listaStanze = [...this.listaStanze];

    setTimeout(() => {
      this.isLoading = false;
      this.cdr.detectChanges();
    }, 150);
  }

  // Separatore di giorno
  isNuovoGiorno(msgCorrente: MessaggioChatDTO, msgPrecedente: MessaggioChatDTO | null): boolean {
    if (!msgCorrente || !msgCorrente.dataInvio) return false;
    if (!msgPrecedente || !msgPrecedente.dataInvio) return true;

    const converti = (d: any): string => {
      if (Array.isArray(d)) return new Date(d[0], d[1] - 1, d[2]).toDateString();
      return new Date(d).toDateString();
    };

    return converti(msgCorrente.dataInvio) !== converti(msgPrecedente.dataInvio);
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
