package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.data.repository.AdminRepository
import com.example.enterprisemobile.model.UtenteBannatoDTO
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AdminRepository = AdminRepository(application)

    // Richieste
    private val _richiestePromozione = MutableLiveData<List<RichiestaPromozioneEntity>>()
    val richiestePromozione: LiveData<List<RichiestaPromozioneEntity>> = _richiestePromozione

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _documentoScaricato = MutableLiveData<ResponseBody?>()
    val documentoScaricato: LiveData<ResponseBody?> = _documentoScaricato

    // Paginazione Richieste
    private val _paginaCorrente = MutableLiveData(0)
    val paginaCorrente: LiveData<Int> = _paginaCorrente

    private val _totalePagine = MutableLiveData(0)
    val totalePagine: LiveData<Int> = _totalePagine

    private val _totaleElementi = MutableLiveData(0)
    val totaleElementi: LiveData<Int> = _totaleElementi

    private var filtroStato: String? = null
    private var filtroUsername: String? = null
    private val dimensionePagina = 10

    // ✅ NUOVO: Utenti bannati con paginazione
    private val _utentiBannati = MutableLiveData<List<UtenteBannatoDTO>>()
    val utentiBannati: LiveData<List<UtenteBannatoDTO>> = _utentiBannati

    private val _paginaCorrenteBan = MutableLiveData(0)
    val paginaCorrenteBan: LiveData<Int> = _paginaCorrenteBan

    private val _totalePagineBan = MutableLiveData(0)
    val totalePagineBan: LiveData<Int> = _totalePagineBan

    private val _totaleElementiBan = MutableLiveData(0)
    val totaleElementiBan: LiveData<Int> = _totaleElementiBan

    private var filtroRicercaBan: String? = null
    private val dimensionePaginaBan = 10

    // === RICHIESTE ===

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
        caricaRichieste(page = 0)
    }

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

    // === UTENTI BANNATI ===

    fun caricaUtentiBannati(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getUtentiBannatiPaginati(
                    page = page,
                    size = dimensionePaginaBan,
                    ricerca = filtroRicercaBan
                )
                if (response.isSuccessful && response.body() != null) {
                    val pageResponse = response.body()!!
                    _utentiBannati.value = pageResponse.content
                    _paginaCorrenteBan.value = pageResponse.number
                    _totalePagineBan.value = pageResponse.totalPages
                    _totaleElementiBan.value = pageResponse.totalElements
                } else {
                    _errorMessage.value = "Errore nel caricamento utenti bannati"
                    _utentiBannati.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore: ${e.message}"
                _utentiBannati.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cercaUtentiBannati(ricerca: String) {
        filtroRicercaBan = ricerca.ifBlank { null }
        caricaUtentiBannati(page = 0)
    }

    fun paginaSuccessivaBan() {
        val corrente = _paginaCorrenteBan.value ?: 0
        val totale = _totalePagineBan.value ?: 0
        if (corrente < totale - 1) {
            caricaUtentiBannati(corrente + 1)
        }
    }

    fun paginaPrecedenteBan() {
        val corrente = _paginaCorrenteBan.value ?: 0
        if (corrente > 0) {
            caricaUtentiBannati(corrente - 1)
        }
    }

    fun riattivaUtente(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.riattivaUtente(id)
                if (response.isSuccessful) {
                    onSuccess()
                    // Ricarica la pagina corrente dopo riattivazione
                    caricaUtentiBannati(_paginaCorrenteBan.value ?: 0)
                } else {
                    onError("Errore nella riattivazione")
                }
            } catch (e: Exception) {
                onError("Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}