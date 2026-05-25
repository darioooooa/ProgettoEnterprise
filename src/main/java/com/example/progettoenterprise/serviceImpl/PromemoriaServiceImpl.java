package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.repositories.PrenotazioneRepository;
import com.example.progettoenterprise.data.service.NotificaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromemoriaServiceImpl {

    private final PrenotazioneRepository prenotazioneRepository;
    private final EmailServiceImpl emailService;
    private final NotificaService notificaService;

    @Scheduled(cron = "0 0 8 * * *")
    public void inviaPromemoriaPartenze() {
        System.out.println("PROMEMORIA SCHEDULER PARTITO");
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(30);

        System.out.println("📅 Intervallo: " + start + " -> " + end);

        List<Prenotazione> daAvvisare =
                prenotazioneRepository.findPrenotazioniPerReminder(start, end,Prenotazione.StatoPrenotazione.CONFERMATA);

        System.out.println("Prenotazioni trovate: " + daAvvisare.size());

        if (daAvvisare.isEmpty()) {
            System.out.println("ℹNessuna prenotazione da notificare");
            return;
        }
        for (Prenotazione p:daAvvisare) {
            try {
                String email = p.getViaggiatore().getEmail();
                long giorniMancanti = ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        p.getViaggio().getDataInizio().toLocalDate()
                );

                String giornoTesto = (giorniMancanti == 1) ? "giorno" : "giorni";
                String corpo = "Ciao " + p.getViaggiatore().getNome()
                        + ", il tuo viaggio per "
                        + p.getViaggio().getDestinazione()
                        + " è tra " + giorniMancanti + " " + giornoTesto + "!";

                System.out.println("Invio email a: " + email);
                emailService.sendSimpleEmail(email, "Promemoria Partenza", corpo);
                notificaService.inviaNotifica(
                        p.getViaggiatore().getId(),
                        "Mancano " + giorniMancanti + " " + giornoTesto + " al tuo viaggio per " + p.getViaggio().getDestinazione() + "!",
                        p.getId()
                );

                System.out.println("Email inviata con successo a " + email);

            } catch (Exception e) {
                System.out.println("ERRORE INVIO EMAIL");
                e.printStackTrace();
            }
        }
    }
}