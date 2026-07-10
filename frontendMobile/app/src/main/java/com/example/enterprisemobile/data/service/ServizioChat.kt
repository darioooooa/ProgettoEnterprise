package com.example.enterprisemobile.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ServizioChat(private val tokenDiAccesso: String) {

    private val clientRete = OkHttpWebSocketClient(creaClientWebSocketSicuro(tokenDiAccesso))
    private val clientStomp = StompClient(clientRete)

    private var sessioneAttiva: StompSession? = null
    private val ambitoCoroutines = CoroutineScope(Dispatchers.IO)

    private val flussoMessaggiInArrivo = MutableSharedFlow<String>()
    val messaggiRicevuti: SharedFlow<String> = flussoMessaggiInArrivo
    private val flussoNotifiche = MutableSharedFlow<String>()
    val notificheGlobali: SharedFlow<String> = flussoNotifiche

    private fun creaClientWebSocketSicuro(token: String): OkHttpClient {
        val gestoreCertificati = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(catena: Array<out X509Certificate>?, tipoAutenticazione: String?) {}
            override fun checkServerTrusted(catena: Array<out X509Certificate>?, tipoAutenticazione: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val contestoSsl = SSLContext.getInstance("SSL")
        contestoSsl.init(null, gestoreCertificati, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(contestoSsl.socketFactory, gestoreCertificati[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { catena ->
                val richiestaAutenticata = catena.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${token.trim()}")
                    .build()
                catena.proceed(richiestaAutenticata)
            }
            .build()
    }

    fun avviaConnessioneGlobale() {
        ambitoCoroutines.launch {
            try {
                val tokenPulito = tokenDiAccesso.trim()
                //CAMBIATEVI IP!!!!!!!!!!
                val indirizzoServerWebsocket = "wss://160.97.172.11:8443/ws?access_token=$tokenPulito"

                sessioneAttiva = clientStomp.connect(
                    url = indirizzoServerWebsocket,
                    customStompConnectHeaders = mapOf("Authorization" to "Bearer $tokenPulito")
                )

                println("Connessione WebSocket stabilita con successo, compa!")
            } catch (eccezione: Exception) {
                println("Errore durante l'apertura del socket: ${eccezione.message}")
            }
        }
    }

    fun iscrivitiAllaStanza(identificativoStanza: Long) {
        ambitoCoroutines.launch {
            sessioneAttiva?.let { sessione ->
                val canaleDestinazione = "/topic/chatroom/$identificativoStanza"
                val iscrizione = sessione.subscribeText(canaleDestinazione)

                iscrizione.collect { nuovoMessaggioTestuale ->
                    flussoMessaggiInArrivo.emit(nuovoMessaggioTestuale)
                }
            }
        }
    }

    fun inviaMessaggio(identificativoStanza: Long, nomeMittente: String, testoMessaggio: String) {
        ambitoCoroutines.launch {
            val destinazioneServer = "/app/chat/invia/$identificativoStanza"

            val corpoMessaggio = """
                {
                    "chatRoomId": $identificativoStanza,
                    "mittenteUsername": "$nomeMittente",
                    "testo": "$testoMessaggio"
                }
            """.trimIndent()

            sessioneAttiva?.sendText(destinazioneServer, corpoMessaggio)
        }
    }
    fun ascoltaNotificheLive(nomeUtente: String) {
        ambitoCoroutines.launch {

            var tentativi = 0
            while (sessioneAttiva == null && tentativi < 10) {
                kotlinx.coroutines.delay(500)
                tentativi++
            }

            sessioneAttiva?.let { sessione ->
                val canale = "/topic/notifiche/$nomeUtente"
                sessione.subscribeText(canale).collect {

                    flussoNotifiche.emit(it)
                }
            }
        }
    }


    fun chiudiConnessione() {
        ambitoCoroutines.launch {
            sessioneAttiva?.disconnect()
            sessioneAttiva = null
        }
    }
}