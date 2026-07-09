package com.example.enterprisemobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.repository.AdminRepository
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.PrenotazioneDTO
import com.example.enterprisemobile.model.ViaggioMappaDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeOrganizzatoreViewModel(
    private val viaggioRepository: ViaggioRepository,
    private val prenotazioneRepository: PrenotazioneRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

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

    private val _filtroStato = MutableStateFlow<String?>(null)
    val filtroStato = _filtroStato.asStateFlow()

    private val _filtroUsername = MutableStateFlow<String?>(null)
    val filtroUsername = _filtroUsername.asStateFlow()

    // Stati per dialog segnalazione
    private val _showSegnalazioneDialog = MutableStateFlow(false)
    val showSegnalazioneDialog: StateFlow<Boolean> = _showSegnalazioneDialog

    private val _viaggiatoreDaSegnalare = MutableStateFlow<Triple<Long, String, String>?>(null)
    val viaggiatoreDaSegnalare: StateFlow<Triple<Long, String, String>?> = _viaggiatoreDaSegnalare

    private val _isLoadingSegnalazione = MutableStateFlow(false)
    val isLoadingSegnalazione: StateFlow<Boolean> = _isLoadingSegnalazione

    fun impostaFiltroStato(stato: String?) {
        _filtroStato.value = stato
        caricaPrenotazioniOrganizzatore(0)
    }

    fun impostaFiltroUsername(username: String?) {
        _filtroUsername.value = username?.takeIf { it.isNotBlank() }
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
                val usernameAttuale = _filtroUsername.value
                val response = prenotazioneRepository.getPrenotazioniPerImeiViaggi(pagina, statoAttuale,usernameAttuale)

                if (response != null) {
                    _prenotazioni.value = response.content ?: emptyList()
                    _totalePagine.value = response.totalPages ?: 1
                } else {
                    _prenotazioni.value = emptyList()
                    _totalePagine.value = 1
                }
                _paginaCorrente.value = pagina
            } catch (e: Exception) {
                android.util.Log.e("PRENOTAZIONI", "Errore caricamento: ${e.message}")
                _prenotazioni.value = emptyList()
                _totalePagine.value = 1
            } finally {
                _isLoadingPrenotazioni.value = false
            }
        }
    }

    //Metodi per segnalazione
    fun apriDialogSegnalazione(viaggiatoreId: Long, viaggiatoreUsername: String, viaggioTitolo: String) {
        _viaggiatoreDaSegnalare.value = Triple(viaggiatoreId, viaggiatoreUsername, viaggioTitolo)
        _showSegnalazioneDialog.value = true
    }

    fun chiudiDialogSegnalazione() {
        _showSegnalazioneDialog.value = false
        _viaggiatoreDaSegnalare.value = null
    }

    fun inviaSegnalazione(
        organizzatoreId: Long,
        motivo: String,
        descrizione: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoadingSegnalazione.value = true
            try {
                val viaggiatore = _viaggiatoreDaSegnalare.value
                if (viaggiatore != null) {
                    val response = adminRepository.creaSegnalazione(
                        tipo = "UTENTE", // Dalla lista prenotazioni si segnala l'Utente
                        idRiferimento = viaggiatore.first,
                        motivo = motivo,
                        descrizione = descrizione,
                        idSegnalatore = organizzatoreId
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                        chiudiDialogSegnalazione()
                    } else {
                        val errorJson = response.errorBody()?.string()
                        val messaggioAvviso = try {
                            org.json.JSONObject(errorJson ?: "").getString("messaggio")
                        } catch (e: Exception) {
                            "Errore nell'invio della segnalazione"
                        }
                        onError(messaggioAvviso)
                    }
                }
            } catch (e: Exception) {
                onError("Errore: ${e.message}")
            } finally {
                _isLoadingSegnalazione.value = false
            }
        }
    }

}