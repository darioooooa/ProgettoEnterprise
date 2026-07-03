package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.data.repository.AdminRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AdminRepository = AdminRepository(application)

    private val _richiestePromozione = MutableLiveData<List<RichiestaPromozioneEntity>>()
    val richiestePromozione: LiveData<List<RichiestaPromozioneEntity>> = _richiestePromozione

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _documentoScaricato = MutableLiveData<ResponseBody?>()
    val documentoScaricato: LiveData<ResponseBody?> = _documentoScaricato

    // Paginazione
    private val _paginaCorrente = MutableLiveData(0)
    val paginaCorrente: LiveData<Int> = _paginaCorrente

    private val _totalePagine = MutableLiveData(0)
    val totalePagine: LiveData<Int> = _totalePagine

    private val _totaleElementi = MutableLiveData(0)
    val totaleElementi: LiveData<Int> = _totaleElementi

    // ✅ NUOVO: Filtri
    private var filtroStato: String? = null
    private var filtroUsername: String? = null
    private val dimensionePagina = 10

    fun caricaRichieste(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getRichiestePromozione(
                    stato = filtroStato,
                    username = filtroUsername,  // ✅ NUOVO
                    page = page,
                    size = dimensionePagina
                )
                _richiestePromozione.value = response.content
                _paginaCorrente.value = response.number
                _totalePagine.value = response.totalPages
                _totaleElementi.value = response.totalElements
            } catch (e: Exception) {
                _errorMessage.value = "Errore di connessione: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filtraPerStato(stato: String?) {
        filtroStato = stato
        caricaRichieste(page = 0)
    }

    // ✅ NUOVO: Ricerca per username
    fun cercaPerUsername(username: String) {
        filtroUsername = username.ifBlank { null }
        caricaRichieste(page = 0)
    }

    fun paginaSuccessiva() {
        val corrente = _paginaCorrente.value ?: 0
        val totale = _totalePagine.value ?: 0
        if (corrente < totale - 1) {
            caricaRichieste(corrente + 1)
        }
    }

    fun paginaPrecedente() {
        val corrente = _paginaCorrente.value ?: 0
        if (corrente > 0) {
            caricaRichieste(corrente - 1)
        }
    }

    fun approvaRichiesta(id: Long, adminId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.approvaRichiesta(id, adminId)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaRichieste(_paginaCorrente.value ?: 0)
                } else {
                    onError("Errore nell'approvazione")
                }
            } catch (e: Exception) {
                onError("Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rifiutaRichiesta(id: Long, motivazione: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.rifiutaRichiesta(id, motivazione)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaRichieste(_paginaCorrente.value ?: 0)
                } else {
                    onError("Errore nel rifiuto: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun scaricaDocumento(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.scaricaDocumento(id)
                if (response.isSuccessful) {
                    _documentoScaricato.value = response.body()
                } else {
                    _errorMessage.value = "Errore nel download del documento"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetDocumentoScaricato() {
        _documentoScaricato.value = null
    }
}