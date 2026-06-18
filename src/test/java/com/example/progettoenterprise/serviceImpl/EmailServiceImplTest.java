package com.example.progettoenterprise.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@enterprise.com");
    }

    @Test
    @DisplayName("Invio Email Semplice: Successo")
    void testSendSimpleEmail() {
        emailService.sendSimpleEmail("utente@email.it", "Oggetto", "Testo dell'email");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Email Ban: Successo")
    void testInviaEmailBan_Successo() {
        emailService.inviaEmailBan("baduser@email.it", "badUser");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Email Ban: Cattura eccezione senza bloccare il sistema")
    void testInviaEmailBan_Errore() {
        // Simuliamo un crash del server di posta
        doThrow(new RuntimeException("Server SMTP offline"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.inviaEmailBan("baduser@email.it", "badUser"));
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Email Sban: Successo")
    void testInviaEmailSban_Successo() {
        emailService.inviaEmailSban("gooduser@email.it", "goodUser");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Email Sban: Cattura eccezione senza bloccare il sistema")
    void testInviaEmailSban_Errore() {
        doThrow(new RuntimeException("Connessione rifiutata"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.inviaEmailSban("gooduser@email.it", "goodUser"));
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Email Avvertimento: Successo")
    void testInviaEmailAvvertimento_Successo() {
        emailService.inviaEmailAvvertimento("warn@email.it", "warnUser", "Messaggio offensivo rimosso");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Email Avvertimento: Cattura eccezione senza bloccare il sistema")
    void testInviaEmailAvvertimento_Errore() {
        doThrow(new RuntimeException("Timeout server"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.inviaEmailAvvertimento("warn@email.it", "warnUser", "Azione rimossa"));
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Invio Email HTML: Successo")
    void testSendHtmlEmail_Successo() throws MessagingException {
        // Per l'HTML, Spring usa un MimeMessage. Dobbiamo simulare la sua creazione.
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        emailService.sendHtmlEmail("html@email.it", "Titolo", "<h1>Ciao</h1>");
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(mimeMessageMock);
    }
}