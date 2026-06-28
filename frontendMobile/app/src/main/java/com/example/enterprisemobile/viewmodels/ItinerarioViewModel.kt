package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
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
}