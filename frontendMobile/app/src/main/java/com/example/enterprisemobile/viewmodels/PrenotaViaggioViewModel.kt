package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.db.ViaggioDAO
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.model.ViaggioEntity
import com.example.enterprisemobile.data.security.SessionManager
import kotlinx.coroutines.launch

class PrenotaViaggioViewModel(application: Application) : AndroidViewModel(application) {

    private val prenotazioneRepository: PrenotazioneRepository
    private val viaggioDao: ViaggioDAO
    var nomeUtente by mutableStateOf("Caricamento...")
    var dettagliViaggio by mutableStateOf<ViaggioEntity?>(null)
    var numeroPersone by mutableIntStateOf(1)
    var prezzoTotale by mutableDoubleStateOf(0.0)

    var isLoading by mutableStateOf(false)
    var mostraModaleConferma by mutableStateOf(false)
    var messaggioErrore by mutableStateOf("")
    var prenotazioneCompletata by mutableStateOf(false)
    var idPrenotazioneCreata by mutableStateOf<Long?>(null)

    init {
        val prenotazioneApi = RetrofitClient.ottieniPrenotazioneService(application)
        viaggioDao = AppDatabase.getInstance(application).viaggioDao()
        val prenotazioneDao = AppDatabase.getInstance(application).prenotazioneDao()
        prenotazioneRepository = PrenotazioneRepository(prenotazioneApi, prenotazioneDao)

        val sessionManager = SessionManager(application)
        nomeUtente = sessionManager.ottieniUsername() ?: "Utente"
    }

    fun caricaDettagliViaggio(viaggioId: Long) {
        viewModelScope.launch {
            try {
                val viaggioTrovato = viaggioDao.getViaggioById(viaggioId)
                if (viaggioTrovato != null) {
                    dettagliViaggio = viaggioTrovato
                    calcolaPrezzo()
                } else {
                    messaggioErrore = "Errore: Viaggio non trovato."
                }
            } catch (e: Exception) {
                messaggioErrore = "Errore nel caricamento: ${e.message}"
            }
        }
    }

    fun confermaPrenotazione(viaggioId: Long) {
        viewModelScope.launch {
            isLoading = true
            messaggioErrore = ""
            try {
                val nuovaPrenotazione = prenotazioneRepository.creaNuovaPrenotazione(viaggioId, numeroPersone)
                idPrenotazioneCreata = nuovaPrenotazione.id

                prenotazioneCompletata = true
                mostraModaleConferma = false
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val testoErrore = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    // Cerchiamo l'etichetta "messaggio" (usata dal tuo ErroreDTO)
                    if (json.has("messaggio")) {
                        json.getString("messaggio")
                    } else if (json.has("message")) {
                        json.getString("message")
                    } else {
                        "Prenotazione rifiutata."
                    }
                } catch (jsonEx: Exception) {
                    "Prenotazione rifiutata dal server."
                }
                messaggioErrore = testoErrore
            } catch (e: Exception) {
                messaggioErrore = "Errore di connessione."
            } finally {
                isLoading = false
            }
        }
    }

    fun aumentaPersone() {
        val postiRimasti = (dettagliViaggio?.maxPartecipanti ?: 0) - (dettagliViaggio?.partecipantiAttuali ?: 0)
        if (numeroPersone < postiRimasti) {
            numeroPersone++
            calcolaPrezzo()
        }
    }

    fun diminuisciPersone() {
        if (numeroPersone > 1) {
            numeroPersone--
            calcolaPrezzo()
        }
    }

    private fun calcolaPrezzo() {
        dettagliViaggio?.let { prezzoTotale = it.prezzo * numeroPersone }
    }

    fun apriModale() {
        mostraModaleConferma = true
    }

    fun chiudiModale() {
        mostraModaleConferma = false
    }
}