package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.UtenteDTO
import com.example.enterprisemobile.model.ViaggioDTO
import kotlinx.coroutines.launch

class ProfiloViewModel(application: Application) : AndroidViewModel(application) {

    private val apiUtenteService = RetrofitClient.ottieniUtenteService(application)
    private val viaggioRepository = ViaggioRepository(
        api = RetrofitClient.ottieniViaggioService(application),
        dao = AppDatabase.getInstance(application).viaggioDao()
    )

    val sessionManager = SessionManager(application)
    var datiProfilo by mutableStateOf<UtenteDTO?>(null)
    var listaViaggiOrganizzati by mutableStateOf<List<ViaggioDTO>>(emptyList())
    var isMioProfilo by mutableStateOf(true)

    var isCaricamento by mutableStateOf(true)
    var messaggioErrore by mutableStateOf<String?>(null)

    private var ultimoIdTarget: Long? = null

    // idDaIntent dice quale profilo caricare
    fun avviaCaricamentoProfilo(idDaIntent: Long) {
        val mioIdInSessione = sessionManager.ottieniIdUtente()?.toLongOrNull()

        if (idDaIntent == -1L || idDaIntent == mioIdInSessione) {
            isMioProfilo = true
            ultimoIdTarget = null
            caricaProfilo(idTarget = null) // Carica il proprio profilo
        } else {
            isMioProfilo = false
            ultimoIdTarget = idDaIntent
            caricaProfilo(idTarget = idDaIntent) // Carica il profilo dell'utente passato
        }
    }

    private fun caricaProfilo(idTarget: Long?) {
        viewModelScope.launch {
            isCaricamento = true
            messaggioErrore = null
            try {
                val rispostaUtente = if (idTarget == null) {
                    apiUtenteService.ottieniMioProfilo()
                } else {
                    apiUtenteService.ottieniProfiloPubblico(idTarget)
                }
                if (rispostaUtente.isSuccessful && rispostaUtente.body() != null) {
                    val profilo = rispostaUtente.body()!!
                    datiProfilo = profilo

                    // Se è un organizzatore si caricano i suoi viaggi
                    if (profilo.ruolo == "ROLE_ORGANIZZATORE") {
                        listaViaggiOrganizzati = viaggioRepository.getViaggiOrganizzatoreConFallback(profilo.id)
                    }
                } else {
                    messaggioErrore = "Impossibile recuperare i dati."
                }
            } catch (e: Exception) {
                e.printStackTrace()

                // Gestione offline
                val idBackup = idTarget ?: sessionManager.ottieniIdUtente()?.toLongOrNull()

                val ruoloProfilo = if (idTarget == null) {
                    sessionManager.ottieniRuolo()
                } else {
                    datiProfilo?.ruolo
                }

                if (idBackup != null && idBackup > 0L && ruoloProfilo == "ROLE_ORGANIZZATORE") {
                    listaViaggiOrganizzati = viaggioRepository.getViaggiOrganizzatoreConFallback(idBackup)
                    messaggioErrore = "Modalità offline: dati caricati dalla cache locale."
                } else {
                    messaggioErrore = "Problema di connessione col server."
                }
            } finally {
                isCaricamento = false
            }
        }
    }

    fun rinfrescaDati() {
        caricaProfilo(ultimoIdTarget)
    }
}