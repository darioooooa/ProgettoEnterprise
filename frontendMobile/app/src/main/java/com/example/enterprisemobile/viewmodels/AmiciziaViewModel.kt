package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.model.AmiciziaDTO
import com.example.enterprisemobile.data.security.SessionManager
import kotlinx.coroutines.launch

class AmiciziaViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.ottieniAmiciziaService(application)
    val mioUsername = SessionManager(application).ottieniUsername() ?: "Utente"

    var listaAmici by mutableStateOf<List<AmiciziaDTO>>(emptyList())
    var richiesteRicevute by mutableStateOf<List<AmiciziaDTO>>(emptyList())
    var richiesteInviate by mutableStateOf<List<AmiciziaDTO>>(emptyList())

    var isLoading by mutableStateOf(false)
    var messaggioAvviso by mutableStateOf<String?>(null)
    var utenteCercato by mutableStateOf<String?>(null)

    fun cercaUtente(username: String) {
        if (username.isNotBlank()) {
            utenteCercato = username.trim()
        }
    }

    fun pulisciRicerca() {
        utenteCercato = null
    }

    init {
        caricaTutto()
    }

    fun caricaTutto() {
        viewModelScope.launch {
            isLoading = true
            try {
                listaAmici = apiService.getMieiAmici()
                richiesteRicevute = apiService.getRichiesteRicevute()
                richiesteInviate = apiService.getRichiesteInviate()
            } catch (e: Exception) {
                messaggioAvviso = "Errore nel caricamento dei dati."
            } finally {
                isLoading = false
            }
        }
    }

    fun inviaRichiesta(usernameDestinatario: String) {
        if (usernameDestinatario.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            messaggioAvviso = null
            try {
                apiService.inviaRichiesta(usernameDestinatario)
                messaggioAvviso = "Richiesta inviata con successo a $usernameDestinatario!"
                richiesteInviate = apiService.getRichiesteInviate() // Aggiorna la lista
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                messaggioAvviso = try {
                    org.json.JSONObject(errorBody ?: "").getString("message")
                } catch (ex: Exception) {
                    "Errore: Utente non trovato o richiesta già esistente."
                }
            } catch (e: Exception) {
                messaggioAvviso = "Errore di connessione."
            } finally {
                isLoading = false
            }
        }
    }

    fun accettaRichiesta(amiciziaId: Long) {
        viewModelScope.launch {
            try {
                apiService.accettaRichiesta(amiciziaId)
                caricaTutto() // Ricarica liste per spostare l'utente tra gli amici
            } catch (e: Exception) {
                messaggioAvviso = "Impossibile accettare."
            }
        }
    }

    fun rifiutaRichiesta(amiciziaId: Long) {
        viewModelScope.launch {
            try {
                apiService.rifiutaRichiesta(amiciziaId)
                caricaTutto()
            } catch (e: Exception) {
                messaggioAvviso = "Impossibile rifiutare."
            }
        }
    }

    fun rimuoviAmico(amicoId: Long) {
        viewModelScope.launch {
            try {
                apiService.rimuoviAmico(amicoId)
                caricaTutto()
            } catch (e: Exception) {
                messaggioAvviso = "Impossibile rimuovere."
            }
        }
    }

    fun pulisciMessaggio() {
        messaggioAvviso = null
    }
}