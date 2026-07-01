package com.example.enterprisemobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MioFirebaseMessagingService : FirebaseMessagingService() {

    // Questo metodo si attiva ogni volta che Google assegna un nuovo Token al telefono
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Nuovo Token Dispositivo: $token")
        // Qui manderai il token a Spring Boot non appena l'utente è loggato
    }

    // Questo si attiva quando arriva una notifica push mentre l'app è in background o chiusa
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val titolo = remoteMessage.notification?.title ?: remoteMessage.data["titolo"] ?: "Aggiornamento Viaggio"
        val corpo = remoteMessage.notification?.body ?: remoteMessage.data["corpo"] ?: "Ci sono novità sul tuo itinerario."

        mostraNotificaSistema(titolo, corpo)
    }

    private fun mostraNotificaSistema(titolo: String, corpo: String) {
        val channelId = "canale_notifiche_enterprise"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Creazione del canale di notifica (Obbligatorio da Android 8 in poi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canale = NotificationChannel(
                channelId,
                "Notifiche Enterprise",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canale per gli avvisi sui viaggi prenotati e preferiti"
            }
            notificationManager.createNotificationChannel(canale)
        }

        // L'intent decide dove mandare l'utente quando clicca sulla notifica (es. alla Home)
        val intent = Intent(this, HomeOrganizzatoreActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Costruzione grafica della notifica nella tendina
        val costruttoreNotifica = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usa un'icona di sistema temporanea
            .setContentTitle(titolo)
            .setContentText(corpo)
            .setAutoCancel(true) // Cancella la notifica dalla tendina quando viene cliccata
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), costruttoreNotifica.build())
    }
}