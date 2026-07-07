package com.example.progettoenterprise.listeners;

import com.example.progettoenterprise.data.service.notificheService.NotificaPushService;
import com.example.progettoenterprise.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

//SERVE PER CENTRALIZZARE LA CATTURA DELLE NOTIFICHE E MANDARLE A SCHERMO SUL TEELFONO
//PER FARE CIO' VERRA' UTILIZZATO IL METODO IMPLEMENTATIVO DEL PATTERN OBSERVER
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificheEventListener {

    private final NotificaPushService notificaPushService;
    // L'annotazione @EventListener fa capire a  Spring da solo che deve
    // far partire questo metodo quando viene lanciato un PrenotazioneCancellataEvent
    @Async
    @EventListener
    public void gestisciPrenotazioneCancellata(PrenotazioneCancellataEvent event) {
        log.info("Ricevuto evento di cancellazione prenotazione. Invio notifica in corso...");

        if (event.getTokenOrganizzatore() != null && !event.getTokenOrganizzatore().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenOrganizzatore(),
                    "Prenotazione annullata ⚠️",
                    "L'utente " + event.getUsernameViaggiatore() + " ha cancellato la prenotazione per " + event.getDestinazioneViaggio() + ". Si sono liberati dei posti!"
            );
        } else {
            log.warn("Impossibile inviare la notifica: l'organizzatore non ha un token registrato.");
        }
    }
    @EventListener
    @Async
    public void gestisciPagamento(PagamentoConfermatoEvent event) {
        log.info("Ricevuto evento di pagamento confermato. Invio notifica...");
        if (event.getTokenViaggiatore() != null && !event.getTokenViaggiatore().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenViaggiatore(),
                    "Prenotazione Confermata! ✅",
                    "Ottima notizia! Il pagamento per il tuo viaggio a " + event.getDestinazioneViaggio() + " è andato a buon fine. Prepara i bagagli!"
            );
        }
        if (event.getTokenOrganizzatore() != null && !event.getTokenOrganizzatore().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenOrganizzatore(),
                    "Nuovo pagamento ricevuto! 💰",
                    "L'utente " + event.getUsernameViaggiatore() + " ha confermato e pagato la prenotazione per " + event.getDestinazioneViaggio() + "."
            );
        }
    }
    @Async
    @EventListener
    public void handlePromemoriaViaggioEvent(PromemoriaViaggioEvent event) {
        notificaPushService.inviaNotificaAUtente(event.getFcmToken(), event.getTitolo(), event.getMessaggio());
    }

    @Async
    @EventListener
    public void gestisciRimborso(RimborsoErogatoEvent event) {
        if (event.getTokenViaggiatore() != null && !event.getTokenViaggiatore().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenViaggiatore(),
                    "Rimborso Emesso! 💸",
                    "Il rimborso per il tuo viaggio '" + event.getNomeViaggio() + "' è stato elaborato con successo e inviato alla tua carta."
            );
        } else {
            log.warn("Impossibile inviare la notifica di rimborso: l'utente non ha un token registrato.");
        }
    }
    @Async
    @EventListener
    public void gestisciNuovaRecensione(NuovaRecensioneEvent event) {

        if (event.getTokenOrganizzatore() != null && !event.getTokenOrganizzatore().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenOrganizzatore(),
                    "Nuova Recensione Ricevuta! ⭐",
                    "L'utente " + event.getUsernameViaggiatore() + " ha appena lasciato una recensione per il tuo itinerario '" + event.getNomeViaggio() + "'."
            );
        } else {
            log.warn("Impossibile inviare la notifica di recensione: l'organizzatore non ha un token Firebase registrato.");
        }
    }
    @Async
    @EventListener
    public void gestisciUtenteSegnalato(SegnalazioneUtenteEvent event) {

        if (event.getTokenUtenteSegnalato() != null && !event.getTokenUtenteSegnalato().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenUtenteSegnalato(),
                    "Avviso di Segnalazione ⚠️",
                    "Il tuo account ha ricevuto una segnalazione. Ti invitiamo a mantenere un comportamento rispettoso delle linee guida della community."
            );
        } else {
            log.warn("⚠️Impossibile inviare l'avviso: l'utente segnalato non ha un token registrato.");
        }
    }
    @Async
    @EventListener
    public void gestisciViaggioConsigliato(ViaggioConsigliatoEvent event) {

        if (event.getTokenViaggiatore() != null && !event.getTokenViaggiatore().trim().isEmpty()) {
            notificaPushService.inviaNotificaAUtente(
                    event.getTokenViaggiatore(),
                    "Ti manca " + event.getCitta() + "? ✈️",
                    "Un nuovo itinerario ('" + event.getTitoloNuovoViaggio() + "') è stato appena pubblicato! Clicca per vedere l'offerta."
            );
            log.info(" Suggerimento inviato con successo a Firebase!");
        } else {
            log.warn("⚠️ Impossibile inviare: token vuoto.");
        }
    }

}
