package com.example.progettoenterprise.data.service.notificheService;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class NotificaPushService {

    public void inviaNotificaAUtente(String tokenDispositivo, String titolo, String corpo) {
        try {
            // Costruiamo la notifica
            Notification notification = Notification.builder()
                    .setTitle(titolo)
                    .setBody(corpo)
                    .build();

            // Impacchettiamo il messaggio legandolo al token del dispositivo di destinazione
            Message messaggio = Message.builder()
                    .setToken(tokenDispositivo)
                    .setNotification(notification)
                    .putData("titolo", titolo) // Dati extra utili per l'app Android
                    .putData("corpo", corpo)
                    .build();

            // Spediamo il tutto a Firebase
            String response = FirebaseMessaging.getInstance().send(messaggio);
            System.out.println("Notifica inviata con successo! ID Risposta: " + response);

        } catch (Exception e) {
            System.err.println("Errore durante l'invio della notifica push: " + e.getMessage());
        }
    }
}
