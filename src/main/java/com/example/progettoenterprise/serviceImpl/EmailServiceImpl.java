package com.example.progettoenterprise.serviceImpl;

import lombok.RequiredArgsConstructor;
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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Comunicazione importante riguardante il tuo account");
        message.setText("Gentile " + username + ",\n\nTi informiamo che il tuo account è stato sospeso per violazione dei termini di servizio.\n\nCordiali saluti,\nIl Team Amministrativo.");

        javaMailSender.send(message);
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
