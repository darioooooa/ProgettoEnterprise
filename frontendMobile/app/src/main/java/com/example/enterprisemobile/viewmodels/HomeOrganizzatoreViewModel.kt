package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.ViaggioMappaDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeOrganizzatoreViewModel(
    private val repository: ViaggioRepository,
    private val isOnline: Boolean // Questo lo passeremo dalla Activity
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<ViaggioMappaDTO>>(emptyList())
    val uiState: StateFlow<List<ViaggioMappaDTO>> = _uiState

    fun caricaDati() {
        if (!isOnline) {
            // Offline: Lista vuota, la mappa sarà pulita
            _uiState.value = emptyList()
            return
        }

        viewModelScope.launch {
            val dati = repository.getViaggiMappa()
            _uiState.value = dati ?: emptyList()
        }
    }
}