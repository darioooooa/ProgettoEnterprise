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