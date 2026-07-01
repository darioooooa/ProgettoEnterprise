package com.example.progettoenterprise.listeners;

import com.example.progettoenterprise.data.NotificaPushService;
import com.example.progettoenterprise.events.PagamentoConfermatoEvent;
import com.example.progettoenterprise.events.PrenotazioneCancellataEvent;
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

}
