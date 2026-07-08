package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.data.repository.AdminRepository
import com.example.enterprisemobile.model.SegnalazioneDTO
import com.example.enterprisemobile.model.UtenteBannatoDTO
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

    private val _paginaCorrente = MutableLiveData(0)
    val paginaCorrente: LiveData<Int> = _paginaCorrente

    private val _totalePagine = MutableLiveData(0)
    val totalePagine: LiveData<Int> = _totalePagine

    private val _totaleElementi = MutableLiveData(0)
    val totaleElementi: LiveData<Int> = _totaleElementi

    private var filtroStato: String? = null
    private var filtroUsername: String? = null
    private val dimensionePagina = 10

    private val _segnalazioni = MutableLiveData<List<SegnalazioneDTO>>()
    val segnalazioni: LiveData<List<SegnalazioneDTO>> = _segnalazioni

    private val _utentiBannati = MutableLiveData<List<UtenteBannatoDTO>>()
    val utentiBannati: LiveData<List<UtenteBannatoDTO>> = _utentiBannati

    // --- METODI RICHIESTE PROMOZIONE ---

    fun caricaRichieste(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getRichiestePromozione(
                    stato = filtroStato,
                    username = filtroUsername,
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
        caricaRichieste(0)
    }

    fun cercaPerUsername(username: String) {
        filtroUsername = username.ifBlank { null }
        caricaRichieste(0)
    }

    fun paginaSuccessiva() {
        if ((_paginaCorrente.value ?: 0) < (_totalePagine.value ?: 0) - 1) caricaRichieste((_paginaCorrente.value ?: 0) + 1)
    }

    fun paginaPrecedente() {
        if ((_paginaCorrente.value ?: 0) > 0) caricaRichieste((_paginaCorrente.value ?: 0) - 1)
    }

    fun approvaRichiesta(id: Long, adminId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.approvaRichiesta(id, adminId)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaRichieste(_paginaCorrente.value ?: 0)
                } else onError("Errore nell'approvazione")
            } catch (e: Exception) { onError("Errore: ${e.message}")
            } finally { _isLoading.value = false }
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
                } else onError("Errore nel rifiuto")
            } catch (e: Exception) { onError("Errore: ${e.message}")
            } finally { _isLoading.value = false }
        }
    }

    fun scaricaDocumento(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.scaricaDocumento(id)
                if (response.isSuccessful) _documentoScaricato.value = response.body()
                else _errorMessage.value = "Errore nel download del documento"
            } catch (e: Exception) { _errorMessage.value = "Errore: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun resetDocumentoScaricato() { _documentoScaricato.value = null }

    fun caricaSegnalazioni() {
        viewModelScope.launch {
            try {
                val response = repository.getSegnalazioni()
                if (response.isSuccessful) _segnalazioni.value = response.body()?.content ?: emptyList()
            } catch (e: Exception) { Log.e("ADMIN_DEBUG", "Errore Segnalazioni: ${e.message}") }
        }
    }

    fun prendiInCaricoSegnalazione(id: Long, adminId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.prendiInCarico(id, adminId)
                if (response.isSuccessful) onSuccess() else onError("Errore presa in carico")
            } catch (e: Exception) { onError(e.message ?: "Errore di rete") }
        }
    }

    fun risolviSegnalazione(id: Long, adminId: Long, sospendiAutore: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.risolviSegnalazione(id, adminId, sospendiAutore)
                if (response.isSuccessful) onSuccess() else onError("Errore risoluzione")
            } catch (e: Exception) { onError(e.message ?: "Errore di rete") }
        }
    }

    fun rifiutaSegnalazione(id: Long, adminId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.rifiutaSegnalazione(id, adminId)
                if (response.isSuccessful) onSuccess() else onError("Errore rifiuto")
            } catch (e: Exception) { onError(e.message ?: "Errore di rete") }
        }
    }


    fun caricaUtentiBannati() {
        viewModelScope.launch {
            try {
                val response = repository.getUtentiBannati()
                if (response.isSuccessful) _utentiBannati.value = response.body() ?: emptyList()
            } catch (e: Exception) { Log.e("ADMIN_DEBUG", "Errore Ban: ${e.message}") }
        }
    }

    fun sbannaUtente(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.riattivaUtente(id)
                if (response.isSuccessful) onSuccess() else onError("Errore sblocco")
            } catch (e: Exception) { onError(e.message ?: "Errore di rete") }
        }
    }
}