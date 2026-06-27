package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.ItinerariApiService
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItinerarioViewModel(private val service: ItinerariApiService) : ViewModel() {


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
                // Chiamata all'API definita in ItinerariApiService
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