package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.UtenteDTO
import kotlinx.coroutines.launch

class ProfiloViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.ottieniUtenteService(application)
    val sessionManager = SessionManager(application)

    var datiProfilo by mutableStateOf<UtenteDTO?>(null)
    var isCaricamento by mutableStateOf(true)
    var messaggioErrore by mutableStateOf<String?>(null)

    init {
        caricaProfilo()
    }

    private fun caricaProfilo() {
        viewModelScope.launch {
            isCaricamento = true
            try {
                val risposta = apiService.ottieniMioProfilo()
                if (risposta.isSuccessful) {
                    datiProfilo = risposta.body()
                } else {
                    messaggioErrore = "Impossibile recuperare i dati."
                }
            } catch (e: Exception) {
                messaggioErrore = "Problema di connessione col server."
            } finally {
                isCaricamento = false
            }
        }
    }
}