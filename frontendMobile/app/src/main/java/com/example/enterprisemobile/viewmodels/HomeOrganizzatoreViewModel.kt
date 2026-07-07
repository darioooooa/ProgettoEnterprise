package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.PrenotazioneDTO
import com.example.enterprisemobile.model.ViaggioMappaDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeOrganizzatoreViewModel(private val viaggioRepository: ViaggioRepository,
                                 private val prenotazioneRepository: PrenotazioneRepository) : ViewModel() {

    private val _viaggi = MutableStateFlow<List<ViaggioMappaDTO>>(emptyList())
    val viaggi: StateFlow<List<ViaggioMappaDTO>> = _viaggi

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _prenotazioni = MutableStateFlow<List<PrenotazioneDTO>>(emptyList())
    val prenotazioni = _prenotazioni.asStateFlow()

    private val _paginaCorrente = MutableStateFlow(0)
    val paginaCorrente = _paginaCorrente.asStateFlow()

    private val _totalePagine = MutableStateFlow(1)
    val totalePagine = _totalePagine.asStateFlow()

    private val _isLoadingPrenotazioni = MutableStateFlow(false)
    val isLoadingPrenotazioni = _isLoadingPrenotazioni.asStateFlow()

    private val _filtroStato= MutableStateFlow<String?>(null)
    val filtroStato= _filtroStato.asStateFlow()

    fun impostaFiltroStato(stato: String?) {
        _filtroStato.value = stato
        caricaPrenotazioniOrganizzatore(0)
    }

    fun caricaDatiMappa() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _viaggi.value = viaggioRepository.getViaggiMappa()
            } catch (e: Exception) {
                android.util.Log.e("MAPPA", "Errore caricamento mappa: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun caricaPrenotazioniOrganizzatore(pagina: Int) {
        viewModelScope.launch {
            _isLoadingPrenotazioni.value = true
            try {
                val statoAttuale = _filtroStato.value
                val response =
                    prenotazioneRepository.getPrenotazioniPerImeiViaggi(pagina, statoAttuale)

                if (response != null) {
                    _prenotazioni.value = response.content ?: emptyList()
                    _totalePagine.value = response.totalPages ?: 1
                } else {
                    // SE IL BACKEND RESTITUISCE NULL, PULIAMO LA SCHERMATA
                    _prenotazioni.value = emptyList()
                    _totalePagine.value = 1
                }
                _paginaCorrente.value = pagina
            } catch (e: Exception) {
                android.util.Log.e("PRENOTAZIONI", "Errore caricamento: ${e.message}")
                // IN CASO DI CRASH DEL SERVER, SVUOTIAMO LA LISTA PER SMASCHERARE L'ERRORE
                _prenotazioni.value = emptyList()
                _totalePagine.value = 1
            } finally {
                _isLoadingPrenotazioni.value = false
            }
        }
    }
}