package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.InterfacciaApiChat
import com.example.enterprisemobile.data.service.ServizioChat
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import com.example.enterprisemobile.data.model.StanzaChatDTO
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val servizioDiChat: ServizioChat,
    private val chiamateApiChat: InterfacciaApiChat
) : ViewModel() {

    private val statoMessaggiInterno = MutableStateFlow<List<MessaggioChatDTO>>(emptyList())
    val messaggiVisibili: StateFlow<List<MessaggioChatDTO>> = statoMessaggiInterno


    private val statoStanzeInterno = MutableStateFlow<List<StanzaChatDTO>>(emptyList())
    val stanzeVisibili: StateFlow<List<StanzaChatDTO>> = statoStanzeInterno

    private val convertitoreJson = Gson()

    init {
        servizioDiChat.avviaConnessioneGlobale()
        ascoltaMessaggiInArrivo()
    }


    fun caricaLeMieStanze(nomeUtente: String) {
        viewModelScope.launch {
            try {
                // Passiamo il nome utente  a Retrofit
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
                // Recuperiamo lo storico dal database e lo mostriamo subito
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
}