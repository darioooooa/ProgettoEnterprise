package com.example.progettoenterprise.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl {

    private final JavaMailSender javaMailSender;

    @Value("${EMAIL_USERNAME}")
    private String fromEmail;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(fromEmail);

        javaMailSender.send(message);
    }


    @Async
    public void inviaEmailRimborsoViaggioEliminato(
            String to,
            String usernameViaggiatore,
            String titoloViaggio,
            String destinazione,
            LocalDate dataInizio,
            LocalDate dataFine,
            double importoRimborso
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("⚠️ Cancellazione Viaggio e Rimborso - " + titoloViaggio);

            String testoEmail = String.format(
                    "Gentile %s,\n\n" +
                            "Siamo spiacenti di informarti che l'organizzatore del viaggio \"%s\" ha deciso di cancellare l'esperienza programmata.\n\n" +
                            "📋 DETTAGLI DEL VIAGGIO CANCELLATO:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "• Titolo: %s\n" +
                            "• Destinazione: %s\n" +
                            "• Data inizio: %s\n" +
                            "• Data fine: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "💰 RIMBORSO\n" +
                            "Il nostro sistema procederà automaticamente ad elaborare il rimborso dell'importo di € %.2f " +
                            "sullo stesso metodo di pagamento utilizzato in fase di prenotazione.\n\n" +
                            "⏱️ TEMPISTICHE\n" +
                            "Il rimborso verrà accreditato entro 5-10 giorni lavorativi, a seconda dei tempi tecnici " +
                            "del tuo istituto bancario o del circuito di pagamento utilizzato.\n\n" +
                            "Ci scusiamo sinceramente per il disagio causato. Comprendiamo quanto possa essere " +
                            "frustrante vedere cancellato un viaggio programmato con anticipo.\n\n" +
                            "Se hai domande o necessiti di assistenza, non esitare a contattare il nostro team " +
                            "di supporto rispondendo a questa email.\n\n" +
                            "Cordiali saluti,\n" +
                            "Il Team di Enterprise",
                    usernameViaggiatore,
                    titoloViaggio,
                    titoloViaggio,
                    destinazione,
                    dataInizio != null ? dataInizio.toString() : "N/D",
                    dataFine != null ? dataFine.toString() : "N/D",
                    importoRimborso
            );

            message.setText(testoEmail);
            message.setFrom(fromEmail);

            javaMailSender.send(message);
            log.info("✅ Email di rimborso inviata a: {} per il viaggio: {}", to, titoloViaggio);
        } catch (Exception e) {
            log.error("❌ Impossibile inviare l'email di rimborso a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void inviaEmailBan(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("⚠️ Il tuo account è stato sospeso");
            message.setText("Gentile " + username + ",\n\n" +
                    "Ti informiamo che il tuo account è stato sospeso per violazione dei termini di servizio.\n\n" +
                    "Se ritieni che si tratti di un errore, puoi contattare il team amministrativo.\n\n" +
                    "Cordiali saluti,\nIl Team di Enterprise.");
            message.setFrom(fromEmail);

            javaMailSender.send(message);
            log.info("Email di BAN inviata con successo a: {}", to);
        } catch (Exception e) {
            log.error("Impossibile inviare l'email di BAN a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void inviaEmailSban(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("✅ Il tuo account è stato riattivato");
            message.setText("Bentornato " + username + ",\n\n" +
                    "Ti informiamo che la sospensione applicata al tuo account è stata revocata. " +
                    "Ora puoi effettuare nuovamente l'accesso e utilizzare tutte le funzionalità della piattaforma.\n\n" +
                    "Cordiali saluti,\nIl Team di Enterprise.");
            message.setFrom(fromEmail);

            javaMailSender.send(message);
            log.info("Email di riattivazione inviata con successo a: {}", to);
        } catch (Exception e) {
            log.error("Impossibile inviare l'email di riattivazione a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void inviaEmailAvvertimento(String to, String username, String azioneRimossa) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("🚩 Avviso di Moderazione");
            message.setText("Gentile " + username + ",\n\n" +
                    "Il nostro team di moderazione ha rilevato un'attività non conforme al nostro regolamento. " +
                    "Di conseguenza, abbiamo dovuto rimuovere il seguente contenuto:\n\n" +
                    "- " + azioneRimossa + "\n\n" +
                    "Ti invitiamo a rispettare le linee guida della community. Ulteriori violazioni potrebbero portare alla sospensione dell'account.\n\n" +
                    "Cordiali saluti,\nIl Team di Enterprise.");
            message.setFrom(fromEmail);

            javaMailSender.send(message);
            log.info("Email di AVVERTIMENTO inviata con successo a: {}", to);
        } catch (Exception e) {
            log.error("Impossibile inviare l'email di AVVERTIMENTO a {}: {}", to, e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.setFrom(fromEmail);

        javaMailSender.send(message);
    }
}