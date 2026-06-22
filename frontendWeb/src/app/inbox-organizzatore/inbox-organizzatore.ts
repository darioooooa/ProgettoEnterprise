import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../service/chat.service';
import { AutenticazioneService } from '../service/autenticazione.service';
import { SegnalazioneService } from '../service/segnalazione.service';
import { MessaggioChatDTO } from '../models/messaggio-chat.model';
import { Subscription } from 'rxjs';
import { ModaleSegnalazione } from '../modale-segnalazione/modale-segnalazione';

@Component({
  selector: 'app-inbox-organizzatore',
  standalone: true,
  imports: [CommonModule, FormsModule, ModaleSegnalazione],
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

  mostraSegnalazione = false;
  idDaSegnalare = 0;

  isLoading: boolean = false;

  private chatSubscription!: Subscription;

  constructor(
    private chatService: ChatService,
    private authService: AutenticazioneService,
    private segnalazioneService: SegnalazioneService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.mioUsername = this.authService.ottieniUsername() || '';

    if (this.mioUsername) {
      this.chatService.InizializzaWebSocketGlobale();
      this.chatService.ascoltaNotificheGlobali(this.mioUsername);


      if (this.chatSubscription) {
        this.chatSubscription.unsubscribe();
      }

      this.chatSubscription = this.chatService.messaggioInArrivo$.subscribe({
        next: (nuovoMsg) => {

          if (this.stanzaSelezionata && nuovoMsg.chatRoomId === this.stanzaSelezionata.id) {
            this.messaggiChat.push(nuovoMsg);
            this.autoscroll();
            this.chatService.segnaComeLetti(this.stanzaSelezionata.id, this.mioUsername).subscribe();
            this.cdr.detectChanges();
          } else {

            const stanzaDaAggiornare = this.stanze.find(s => s.id === nuovoMsg.chatRoomId);
            if (stanzaDaAggiornare) {
              stanzaDaAggiornare.messaggiNonLetti = (stanzaDaAggiornare.messaggiNonLetti || 0) + 1;
              this.cdr.detectChanges();
            }


            this.chatService.ottieniNotificheTotali(this.mioUsername).subscribe({
              next: (totale) => {
                this.chatService.aggiornaContatoreNotifiche(totale);
                this.cdr.detectChanges();
              }
            });
          }
        },
        error: (err) => console.error("Errore ricezione live:", err)
      });

      this.caricaListaStanze();
    }
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
    if (this.isLoading) return;

    this.isLoading = true;
    this.stanzaSelezionata = stanza;
    this.messaggiChat = [];


    stanza.messaggiNonLetti = 0;

    this.chatService.segnaComeLetti(stanza.id, this.mioUsername).subscribe({
      next: () => {
        this.chatService.ottieniNotificheTotali(this.mioUsername).subscribe({
          next: (totale) => {
            this.chatService.aggiornaContatoreNotifiche(totale);
          }
        });

        this.chatService.ottieniCronologia(stanza.id).subscribe({
          next: (storico) => {
            this.messaggiChat = storico;
            this.autoscroll();
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error("Errore nel recupero dello storico:", err);
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        console.error("Errore aggiornamento stato letto:", err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });

    // Connette il canale  specifico per inviare messaggi su questa stanza
    this.chatService.connettiEIniziaAscolto(stanza.id);
  }

  inviaMessaggio(): void {
    if (!this.nuovoMessaggioTesto.trim() || !this.stanzaSelezionata || this.isLoading) return;

    this.isLoading = true;

    const dto: MessaggioChatDTO = {
      chatRoomId: this.stanzaSelezionata.id,
      mittenteUsername: this.mioUsername,
      testo: this.nuovoMessaggioTesto.trim()
    };

    this.chatService.inviaMessaggio(this.stanzaSelezionata.id, dto);
    this.nuovoMessaggioTesto = '';

    setTimeout(() => {
      this.isLoading = false;
      this.cdr.detectChanges();
    }, 150);
  }

  apriSegnalazione(id: number) {
    if (this.isLoading) return;
    this.idDaSegnalare = id;
    this.mostraSegnalazione = true;
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
  }

  ngOnDestroy(): void {
    this.disconnettiChatCorrente();
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }
  }
}
