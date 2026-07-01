package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import com.example.enterprisemobile.model.Visibilita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItinerarioViewModel(application: Application) : AndroidViewModel(application) {

    private val service = RetrofitClient.ottieniItinerariService(application)

    private val _itinerari = MutableStateFlow<List<ItinerarioPreferitoDTO>>(emptyList())
    val itinerari: StateFlow<List<ItinerarioPreferitoDTO>> = _itinerari.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun caricaItinerari() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val risposta = service.ottieniMieListe()
                _itinerari.value = risposta
            } catch (e: Exception) {
                _errorMessage.value = "Errore nel caricamento: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun creaItinerario(nomeItinerario: String, visibilitaScelta: Visibilita) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {

                val nuovoDto = ItinerarioPreferitoDTO(
                    nome = nomeItinerario,
                    visibilita = visibilitaScelta,

                    idItinerario = null,
                    inCondivisione = false,
                    proprietarioUsername = null,
                    dataCreazione = null,
                    viaggiContenuti = emptyList()
                )

                // Chiamata all'API
                val itinerarioCreato = service.creaNuovoItinerario(nuovoDto)

                // Aggiungiamo il nuovo itinerario alla lista attuale
                val listaAttuale = _itinerari.value.toMutableList()
                listaAttuale.add(itinerarioCreato)
                _itinerari.value = listaAttuale

            } catch (e: Exception) {
                _errorMessage.value = "Errore durante la creazione: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun avviaEliminazioneItinerario(idDaCancellare: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                //invia eliminazione backend
                service.cancellaItinerarioDalServer(idDaCancellare)


                val listaSenzaElementoCancellato = _itinerari.value.toMutableList()
                listaSenzaElementoCancellato.removeAll { it.idItinerario == idDaCancellare }
                _itinerari.value = listaSenzaElementoCancellato

            } catch (erroreDiRete: Exception) {
                _errorMessage.value = "Errore durante l'eliminazione: ${erroreDiRete.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }




}