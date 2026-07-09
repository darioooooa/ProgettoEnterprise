package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.InterfacciaApiChat
import com.example.enterprisemobile.data.service.ServizioChat
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import com.example.enterprisemobile.data.model.RichiestaSegnalazioneDTO
import com.example.enterprisemobile.data.model.StanzaChatDTO
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
class ChatViewModel(
    private val servizioDiChat: ServizioChat,
    private val chiamateApiChat: InterfacciaApiChat
) : ViewModel() {

    private val statoMessaggiInterno = MutableStateFlow<List<MessaggioChatDTO>>(emptyList())
    val messaggiVisibili: StateFlow<List<MessaggioChatDTO>> = statoMessaggiInterno

    private val statoStanzeInterno = MutableStateFlow<List<StanzaChatDTO>>(emptyList())
    val stanzeVisibili: StateFlow<List<StanzaChatDTO>> = statoStanzeInterno

    private val convertitoreJson = Gson()

    // Stati per messaggi di avviso (successo/errore)
    var messaggioAvviso by mutableStateOf<String?>(null)
        private set

    var tipoAvviso by mutableStateOf("errore")
        private set

    init {
        servizioDiChat.avviaConnessioneGlobale()
        ascoltaMessaggiInArrivo()
    }

    fun caricaLeMieStanze(nomeUtente: String) {
        viewModelScope.launch {
            try {
                val stanzeRecuperate = chiamateApiChat.caricaLeMieStanze(nomeUtente)
                statoStanzeInterno.value = stanzeRecuperate
            } catch (erroreDiRete: Exception) {
                println("Impossibile caricare le stanze: ${erroreDiRete.message}")
            }
        }
    }

    private fun ascoltaMessaggiInArrivo() {
        viewModelScope.launch {
            servizioDiChat.messaggiRicevuti.collect { nuovoMessaggioGrezzo ->
                try {
                    val messaggioTradotto = convertitoreJson.fromJson(
                        nuovoMessaggioGrezzo,
                        MessaggioChatDTO::class.java
                    )

                    val listaAggiornata = statoMessaggiInterno.value.toMutableList()
                    listaAggiornata.add(messaggioTradotto)
                    statoMessaggiInterno.value = listaAggiornata
                } catch (erroreDiConversione: Exception) {
                    println("Impossibile tradurre il messaggio: ${erroreDiConversione.message}")
                }
            }
        }
    }

    fun entraNellaStanza(identificativoStanza: Long) {
        viewModelScope.launch {
            try {
                val cronologiaPassata = chiamateApiChat.ottieniCronologiaStorica(identificativoStanza)
                statoMessaggiInterno.value = cronologiaPassata
            } catch (erroreDiRete: Exception) {
                println("Impossibile caricare lo storico: ${erroreDiRete.message}")
            }

            servizioDiChat.iscrivitiAllaStanza(identificativoStanza)
        }
    }

    fun inviaIlTuoMessaggio(identificativoStanza: Long, nomeMittente: String, testoMessaggio: String) {
        servizioDiChat.inviaMessaggio(identificativoStanza, nomeMittente, testoMessaggio)
    }

    override fun onCleared() {
        super.onCleared()
        servizioDiChat.chiudiConnessione()
    }

    fun azzeraNotificheStanza(identificativoStanza: Long, nomeUtente: String) {
        viewModelScope.launch {
            try {
                chiamateApiChat.segnaMessaggiComeLetti(identificativoStanza, nomeUtente)
                caricaLeMieStanze(nomeUtente)
            } catch (erroreDiRete: Exception) {
                println("Impossibile azzerare le notifiche: ${erroreDiRete.message}")
            }
        }
    }

    fun azzeraNotificheStanzaOrganizzatore(identificativoStanza: Long, nomeUtente: String) {
        viewModelScope.launch {
            try {
                chiamateApiChat.segnaMessaggiComeLetti(identificativoStanza, nomeUtente)
                caricaLeMieStanzeOrganizzatore(nomeUtente)
            } catch (erroreDiRete: Exception) {
                println("Impossibile azzerare le notifiche organizzatore: ${erroreDiRete.message}")
            }
        }
    }

    fun attivaAscoltoNotifiche(nomeUtente: String) {
        servizioDiChat.ascoltaNotificheLive(nomeUtente)

        viewModelScope.launch {
            servizioDiChat.notificheGlobali.collect {
                caricaLeMieStanze(nomeUtente)
            }
        }
    }

    fun attivaAscoltoNotificheOrganizzatore(nomeUtente: String) {
        servizioDiChat.ascoltaNotificheLive(nomeUtente)

        viewModelScope.launch {
            servizioDiChat.notificheGlobali.collect {
                caricaLeMieStanzeOrganizzatore(nomeUtente)
            }
        }
    }

    fun caricaLeMieStanzeOrganizzatore(nomeUtente: String) {
        viewModelScope.launch {
            try {
                val stanzeRecuperate = chiamateApiChat.caricaStanzeOrganizzatore(nomeUtente)
                statoStanzeInterno.value = stanzeRecuperate
            } catch (erroreDiRete: Exception) {
                println("Errore nel caricamento delle stanze organizzatore: ${erroreDiRete.message}")
            }
        }
    }

    // Metodo per azzerare il messaggio di avviso
    fun azzeraMessaggioAvviso() {
        messaggioAvviso = null
    }

    fun inviaSegnalazioneMessaggio(
        identificativoMessaggio: Long,
        idUtenteSegnalatore: Long,
        motivo: String,
        descrizione: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val pacchettoSegnalazione = RichiestaSegnalazioneDTO(
                    tipoEntita = "MESSAGGIO",
                    identificativoDiRiferimento = identificativoMessaggio,
                    motivazioneSelezionata = motivo,
                    descrizioneDettagliata = descrizione
                )

                val risposta = chiamateApiChat.creaSegnalazione(
                    idUtenteSegnalatore = idUtenteSegnalatore,
                    richiesta = pacchettoSegnalazione
                )

                if (risposta.isSuccessful) {
                    // SUCCESSO: Mostra messaggio verde
                    messaggioAvviso = "Segnalazione inviata con successo!"
                    tipoAvviso = "successo"
                    onSuccess()
                } else {
                    // ERRORE: Estrae il messaggio dal JSON (es. "Hai già segnalato...")
                    val errorJson = risposta.errorBody()?.string()
                    val messaggioAvvisoEstratto = try {
                        org.json.JSONObject(errorJson ?: "").getString("messaggio")
                    } catch (e: Exception) {
                        "Errore nell'invio della segnalazione"
                    }

                    // Aggiorna gli stati per mostrare il banner
                    messaggioAvviso = messaggioAvvisoEstratto
                    tipoAvviso = "errore"

                    onError(messaggioAvvisoEstratto)
                }

            } catch (erroreDiRete: Exception) {
                val messaggioErrore = "Impossibile contattare il server: ${erroreDiRete.message}"
                messaggioAvviso = messaggioErrore
                tipoAvviso = "errore"
                onError(messaggioErrore)
            }
        }
    }
}