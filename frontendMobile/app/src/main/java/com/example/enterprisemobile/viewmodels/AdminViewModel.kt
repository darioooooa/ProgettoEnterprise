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

    // === RICHIESTE PROMOZIONE ===

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

    // === SEGNALAZIONI ===

    private val _segnalazioni = MutableLiveData<List<SegnalazioneDTO>>()
    val segnalazioni: LiveData<List<SegnalazioneDTO>> = _segnalazioni

    private val _isLoadingSegnalazioni = MutableLiveData<Boolean>()
    val isLoadingSegnalazioni: LiveData<Boolean> = _isLoadingSegnalazioni

    // Stati paginazione segnalazioni
    private val _paginaCorrenteSegnalazioni = MutableLiveData(0)
    val paginaCorrenteSegnalazioni: LiveData<Int> = _paginaCorrenteSegnalazioni

    private val _totalePagineSegnalazioni = MutableLiveData(0)
    val totalePagineSegnalazioni: LiveData<Int> = _totalePagineSegnalazioni

    private val _totaleElementiSegnalazioni = MutableLiveData(0)
    val totaleElementiSegnalazioni: LiveData<Int> = _totaleElementiSegnalazioni

    // Filtri segnalazioni
    private var filtroTipoSegnalazione: String? = null
    private var filtroUsernameSegnalazione: String? = null
    private var filtroStatiSegnalazione: List<String>? = null
    private val dimensionePaginaSegnalazioni = 10

    // === UTENTI BANNATI ===

    private val _utentiBannati = MutableLiveData<List<UtenteBannatoDTO>>()
    val utentiBannati: LiveData<List<UtenteBannatoDTO>> = _utentiBannati

    // === METODI RICHIESTE ===

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
        if ((_paginaCorrente.value ?: 0) < (_totalePagine.value ?: 0) - 1) {
            caricaRichieste((_paginaCorrente.value ?: 0) + 1)
        }
    }

    fun paginaPrecedente() {
        if ((_paginaCorrente.value ?: 0) > 0) {
            caricaRichieste((_paginaCorrente.value ?: 0) - 1)
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
                } else onError("Errore nell'approvazione")
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
                } else onError("Errore nel rifiuto")
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
                if (response.isSuccessful) _documentoScaricato.value = response.body()
                else _errorMessage.value = "Errore nel download del documento"
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

    // === METODI SEGNALAZIONI ===

    fun caricaSegnalazioni(
        tipo: String? = filtroTipoSegnalazione,
        username: String? = filtroUsernameSegnalazione,
        stati: List<String>? = filtroStatiSegnalazione,
        page: Int = _paginaCorrenteSegnalazioni.value ?: 0
    ) {
        viewModelScope.launch {
            _isLoadingSegnalazioni.value = true
            try {
                val response = repository.getSegnalazioni(
                    tipo = tipo,
                    usernameSegnalatore = username,
                    page = page,
                    size = dimensionePaginaSegnalazioni,
                    stati = stati  // ✅ Passa gli stati
                )
                if (response.isSuccessful) {
                    val pageResponse = response.body()
                    _segnalazioni.value = pageResponse?.content ?: emptyList()
                    _paginaCorrenteSegnalazioni.value = pageResponse?.number ?: 0
                    _totalePagineSegnalazioni.value = pageResponse?.totalPages ?: 0
                    _totaleElementiSegnalazioni.value = pageResponse?.totalElements ?: 0
                } else {
                    _errorMessage.value = "Errore caricamento segnalazioni: ${response.code()}"
                    _segnalazioni.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ADMIN_DEBUG", "Errore Segnalazioni: ${e.message}")
                _errorMessage.value = "Errore: ${e.message}"
                _segnalazioni.value = emptyList()
            } finally {
                _isLoadingSegnalazioni.value = false
            }
        }
    }

    fun filtraSegnalazioniPerTipo(tipo: String?) {
        filtroTipoSegnalazione = tipo
        _paginaCorrenteSegnalazioni.value = 0
        caricaSegnalazioni(tipo = tipo, page = 0)
    }

    fun cercaSegnalazioniPerUsername(username: String) {
        filtroUsernameSegnalazione = username.ifBlank { null }
        _paginaCorrenteSegnalazioni.value = 0
        caricaSegnalazioni(username = filtroUsernameSegnalazione, page = 0)
    }

    fun impostaStatiSegnalazioni(mostraArchivio: Boolean) {
        filtroStatiSegnalazione = if (mostraArchivio) {
            listOf("CHIUSA", "RIFIUTATA")
        } else {
            listOf("APERTA", "IN_LAVORAZIONE")
        }
        _paginaCorrenteSegnalazioni.value = 0
        caricaSegnalazioni(stati = filtroStatiSegnalazione, page = 0)
    }

    // Metodi paginazione segnalazioni
    fun paginaSuccessivaSegnalazioni() {
        val corrente = _paginaCorrenteSegnalazioni.value ?: 0
        val totale = _totalePagineSegnalazioni.value ?: 0
        if (corrente < totale - 1) {
            caricaSegnalazioni(page = corrente + 1)
        }
    }

    fun paginaPrecedenteSegnalazioni() {
        val corrente = _paginaCorrenteSegnalazioni.value ?: 0
        if (corrente > 0) {
            caricaSegnalazioni(page = corrente - 1)
        }
    }

    fun prendiInCaricoSegnalazione(id: Long, adminId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.prendiInCarico(id, adminId)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaSegnalazioni()
                } else onError("Errore presa in carico")
            } catch (e: Exception) {
                onError(e.message ?: "Errore di rete")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun risolviSegnalazione(id: Long, adminId: Long, sospendiAutore: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.risolviSegnalazione(id, adminId, sospendiAutore)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaSegnalazioni()
                } else onError("Errore risoluzione")
            } catch (e: Exception) {
                onError(e.message ?: "Errore di rete")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rifiutaSegnalazione(id: Long, adminId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.rifiutaSegnalazione(id, adminId)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaSegnalazioni()
                } else onError("Errore rifiuto")
            } catch (e: Exception) {
                onError(e.message ?: "Errore di rete")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === METODI UTENTI BANNATI ===

    fun caricaUtentiBannati() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getUtentiBannati()
                if (response.isSuccessful) _utentiBannati.value = response.body() ?: emptyList()
            } catch (e: Exception) {
                Log.e("ADMIN_DEBUG", "Errore Ban: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sbannaUtente(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.riattivaUtente(id)
                if (response.isSuccessful) {
                    onSuccess()
                    caricaUtentiBannati()
                } else onError("Errore sblocco")
            } catch (e: Exception) {
                onError(e.message ?: "Errore di rete")
            } finally {
                _isLoading.value = false
            }
        }
    }
}