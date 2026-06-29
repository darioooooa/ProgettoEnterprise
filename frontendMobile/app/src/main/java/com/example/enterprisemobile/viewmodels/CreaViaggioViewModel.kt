package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.CreaViaggioDTO
import com.example.enterprisemobile.model.TappaDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

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

class CreaViaggioViewModel(private val repository: ViaggioRepository) : ViewModel() {

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
            tappe = tappeDTO
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