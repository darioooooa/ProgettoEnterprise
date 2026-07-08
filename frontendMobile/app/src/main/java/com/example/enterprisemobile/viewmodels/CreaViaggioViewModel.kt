package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.CreaViaggioDTO
import com.example.enterprisemobile.model.TappaDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

sealed class CreaViaggioState {
    object Idle : CreaViaggioState()
    object Loading : CreaViaggioState()
    object Success : CreaViaggioState()
    data class Error(val message: String) : CreaViaggioState()
}

data class TappaState(
    val id: String = UUID.randomUUID().toString(),
    val titoloTappa: String = "",
    val costo: String = "",
    val posizione: String = "",
    val orarioInizio: String = "",
    val orarioFine: String = "",
    val descrizioneTappa: String = ""
)

class CreaViaggioViewModel(
    application: Application,
    private val repository: ViaggioRepository
) : AndroidViewModel(application) {

    var titolo = MutableStateFlow("")
    var descrizione = MutableStateFlow("")
    var partenza = MutableStateFlow("")
    var destinazione = MutableStateFlow("")
    var prezzo = MutableStateFlow("")
    var dataInizio = MutableStateFlow("")
    var dataFine = MutableStateFlow("")
    var postiDisponibili = MutableStateFlow("")

    private val _tappe = MutableStateFlow<List<TappaState>>(emptyList())
    val tappe: StateFlow<List<TappaState>> = _tappe.asStateFlow()

    private val _uiState = MutableStateFlow<CreaViaggioState>(CreaViaggioState.Idle)
    val uiState: StateFlow<CreaViaggioState> = _uiState.asStateFlow()

    private val _tagDisponibili = MutableStateFlow<List<String>>(emptyList())
    val tagDisponibili: StateFlow<List<String>> = _tagDisponibili.asStateFlow()

    private val _tagSelezionati = MutableStateFlow<Set<String>>(emptySet())
    val tagSelezionati: StateFlow<Set<String>> = _tagSelezionati.asStateFlow()

    private val _isLoadingTag = MutableStateFlow(false)
    val isLoadingTag: StateFlow<Boolean> = _isLoadingTag.asStateFlow()

    init {
        caricaTagDisponibili()
    }

    fun caricaTagDisponibili() {
        viewModelScope.launch {
            _isLoadingTag.value = true
            try {
                val apiService = RetrofitClient.ottieniViaggioService(getApplication())
                val response = apiService.getAllTag()
                if (response.isSuccessful && response.body() != null) {
                    _tagDisponibili.value = response.body()!!
                }
            } catch (e: Exception) {
                android.util.Log.e("TAG", "Errore caricamento tag: ${e.message}")
            } finally {
                _isLoadingTag.value = false
            }
        }
    }

    fun toggleTag(tag: String) {
        val attuali = _tagSelezionati.value.toMutableSet()
        if (attuali.contains(tag)) {
            attuali.remove(tag)
        } else {
            if (attuali.size >= 3) {
                return
            }
            attuali.add(tag)
        }
        _tagSelezionati.value = attuali
    }

    fun aggiungiTappa() {
        _tappe.value = _tappe.value + TappaState()
    }

    fun rimuoviTappa(id: String) {
        _tappe.value = _tappe.value.filter { it.id != id }
    }

    fun aggiornaTappa(id: String, nuovaTappa: TappaState) {
        _tappe.value = _tappe.value.map { if (it.id == id) nuovaTappa else it }
    }

    fun salvaViaggio(context: Context) {

        if (_tagSelezionati.value.isEmpty()) {
            _uiState.value = CreaViaggioState.Error("Seleziona almeno 1 tag per il viaggio")
            return
        }
        if (titolo.value.isBlank() || destinazione.value.isBlank()) {
            _uiState.value = CreaViaggioState.Error("Compila almeno titolo e destinazione")
            return
        }

        _uiState.value = CreaViaggioState.Loading

        viewModelScope.launch {
            var latCalcolata = 0.0
            var lngCalcolata = 0.0
            try {
                withContext(Dispatchers.IO) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val indirizzi = geocoder.getFromLocationName(destinazione.value, 1)
                    if (!indirizzi.isNullOrEmpty()) {
                        latCalcolata = indirizzi[0].latitude
                        lngCalcolata = indirizzi[0].longitude
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val tappeDTO = _tappe.value.map {
                TappaDTO(
                    titolo = it.titoloTappa,
                    costo = it.costo.toDoubleOrNull() ?: 0.0,
                    posizione = it.posizione,
                    orarioInizio = it.orarioInizio,
                    orarioFine = it.orarioFine,
                    descrizione = it.descrizioneTappa
                )
            }

            val nuovoViaggio = CreaViaggioDTO(
                titolo = titolo.value,
                descrizione = descrizione.value,
                cittaPartenza = partenza.value,
                destinazione = destinazione.value,
                prezzo = prezzo.value.toDoubleOrNull() ?: 0.0,
                dataInizio = dataInizio.value,
                dataFine = dataFine.value,
                maxPartecipanti = postiDisponibili.value.toIntOrNull() ?: 0,
                latitudine = latCalcolata,
                longitudine = lngCalcolata,
                tappe = tappeDTO,
                tags = _tagSelezionati.value.toList()
            )

            val result = repository.creaViaggio(nuovoViaggio)
            if (result.isSuccess) {
                _uiState.value = CreaViaggioState.Success
            } else {
                _uiState.value = CreaViaggioState.Error(result.exceptionOrNull()?.message ?: "Errore")
            }
        }
    }
}