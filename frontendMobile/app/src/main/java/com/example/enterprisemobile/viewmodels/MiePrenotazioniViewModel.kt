package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.PrenotazioneEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MiePrenotazioniViewModel(application: Application) : AndroidViewModel(application) {
    var nomeUtente by mutableStateOf("Utente")
    var tabSelezionata by mutableIntStateOf(0)
    var isCaricamento by mutableStateOf(false)
    private val _prenotazioniInProgramma = MutableStateFlow<List<PrenotazioneEntity>>(emptyList())
    val prenotazioniInProgramma: StateFlow<List<PrenotazioneEntity>> = _prenotazioniInProgramma

    private val _prenotazioniCompletate = MutableStateFlow<List<PrenotazioneEntity>>(emptyList())
    val prenotazioniCompletate: StateFlow<List<PrenotazioneEntity>> = _prenotazioniCompletate

    private val repository: PrenotazioneRepository

    init {
        val sessionManager = SessionManager(application)
        nomeUtente = sessionManager.ottieniUsername() ?: "Utente"
        val api = RetrofitClient.ottieniPrenotazioneService(application)
        val dao = AppDatabase.getInstance(application).prenotazioneDao()
        repository = PrenotazioneRepository(api, dao)

        scaricaEAggiornaPrenotazioni()
    }

    private fun scaricaEAggiornaPrenotazioni() {
        viewModelScope.launch {
            isCaricamento = true

            repository.caricaMiePrenotazioniDaServer(page = 0)

            val prenotazioniLocali = repository.getMiePrenotazioniLocali()

            dividiPrenotazioni(prenotazioniLocali)

            isCaricamento = false
        }
    }

    private fun dividiPrenotazioni(tutteLePrenotazioni: List<PrenotazioneEntity>) {
        val inProgramma = mutableListOf<PrenotazioneEntity>()
        val completate = mutableListOf<PrenotazioneEntity>()
        val oggi = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (prenotazione in tutteLePrenotazioni) {
            try {
                val dataFine = prenotazione.viaggioDataFine?.let { LocalDate.parse(it, formatter) }
                if (dataFine != null && dataFine.isBefore(oggi)) {
                    completate.add(prenotazione)
                } else {
                    inProgramma.add(prenotazione)
                }
            } catch (e: Exception) {
                inProgramma.add(prenotazione)
            }
        }

        _prenotazioniInProgramma.value = inProgramma
        _prenotazioniCompletate.value = completate
    }
}