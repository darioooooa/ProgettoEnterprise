package com.example.progettoenterprise.data.service.notificheService;

import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.service.ViaggioService;
import com.example.progettoenterprise.events.PromemoriaViaggioEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
//sserve per svegliarsi ogni volta alle 10 e controllare se per l'utenteloggato ci sono viaggi
//che a breve partiranno e quindi controlla il db e lancia il PromemoriaViaggioEvent
public class NotificaPromemoriaService {
    private final ViaggioService viaggioService;

    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Rome")
    @Transactional(readOnly = true)
    public void controllaEInviaPromemoriaViaggi() {

        LocalDate oggi = LocalDate.now();
        LocalDate limiteQuattroGiorni = oggi.plusDays(4);

        List<Viaggio> viaggiInPartenza = viaggioService.getViaggiInPartenza(oggi, limiteQuattroGiorni);

        for (Viaggio viaggio : viaggiInPartenza) {
            long giorniMancanti = ChronoUnit.DAYS.between(oggi, viaggio.getDataInizio());
            String messaggio = formattaMessaggioPromemoria(giorniMancanti, viaggio.getTitolo());

            for (Prenotazione prenotazione : viaggio.getPrenotazioniRicevute()) {
                String token = prenotazione.getViaggiatore().getFirebaseToken();


                if (token != null && !token.isEmpty()) {
                    PromemoriaViaggioEvent evento = new PromemoriaViaggioEvent(token, "Promemoria Viaggio", messaggio);
                    eventPublisher.publishEvent(evento);
                    log.info("Push Android inviato a {} per il viaggio '{}'", prenotazione.getViaggiatore().getUsername(), viaggio.getTitolo());
                }
            }
        }
    }

    private String formattaMessaggioPromemoria(long giorniMancanti, String titoloViaggio) {
        if (giorniMancanti == 0) {
            return "È il grande giorno! Oggi parti per '" + titoloViaggio + "'. Buon viaggio!";
        } else if (giorniMancanti == 1) {
            return "Manca solo 1 giorno alla partenza per '" + titoloViaggio + "'! Preparati!";
        } else {
            return "Mancano " + giorniMancanti + " giorni alla tua partenza per '" + titoloViaggio + "'.";
        }
    }
}
