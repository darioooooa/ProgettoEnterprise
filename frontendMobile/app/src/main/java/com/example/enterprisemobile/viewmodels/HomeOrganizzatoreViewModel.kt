package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.ViaggioMappaDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeOrganizzatoreViewModel(private val repository: ViaggioRepository) : ViewModel() {

    private val _viaggi = MutableStateFlow<List<ViaggioMappaDTO>>(emptyList())
    val viaggi: StateFlow<List<ViaggioMappaDTO>> = _viaggi

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun caricaDatiMappa() {
        viewModelScope.launch {
            _isLoading.value = true
            _viaggi.value = repository.getViaggiMappa() // Delegato al repository
            _isLoading.value = false
        }
    }
}