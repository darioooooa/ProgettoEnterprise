package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.ProgrammaRepository
import com.example.enterprisemobile.model.AttivitaViaggioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgrammaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProgrammaRepository

    private val _attivita = MutableStateFlow<List<AttivitaViaggioEntity>>(emptyList())
    val attivita: StateFlow<List<AttivitaViaggioEntity>> = _attivita

    // Stati generici
    var isLoading by mutableStateOf(false)
    var messaggioAvviso by mutableStateOf<String?>(null)
    var tipoAvviso by mutableStateOf("errore")
    var paginaAttivita by mutableIntStateOf(0)
    var totalePagineAttivita by mutableIntStateOf(1)

    // Form di inserimento / modifica
    var titoloInput by mutableStateOf("")
    var posizioneInput by mutableStateOf("")
    var orarioInizioInput by mutableStateOf("")
    var orarioFineInput by mutableStateOf("")
    var costoInput by mutableStateOf("")
    var descrizioneInput by mutableStateOf("")

    var attivitaInModifica by mutableStateOf(false)
    var idAttivitaInModifica by mutableStateOf<Long?>(null)
    var idAttivitaDaEliminare by mutableStateOf<Long?>(null)

    // Variabili per i filtri
    var filtroTitolo by mutableStateOf("")
    var filtroPosizione by mutableStateOf("")
    var filtroCostoMin by mutableStateOf("")
    var filtroCostoMax by mutableStateOf("")
    var filtroOrarioInizioMin by mutableStateOf("")
    var filtroOrarioInizioMax by mutableStateOf("")
    var filtroOrarioFineMin by mutableStateOf("")
    var filtroOrarioFineMax by mutableStateOf("")

    init {
        val api = RetrofitClient.ottieniViaggioService(application)
        val dao = AppDatabase.getInstance(application).programmaDao()
        repository = ProgrammaRepository(api, dao)
    }

    fun caricaAttivita(viaggioId: Long) {
        viewModelScope.launch {
            isLoading = true

            _attivita.value = emptyList()

            val mappaFiltri = mutableMapOf<String, String>()

            if (filtroTitolo.isNotBlank()) mappaFiltri["titolo"] = filtroTitolo.trim()
            if (filtroPosizione.isNotBlank()) mappaFiltri["posizione"] = filtroPosizione.trim()
            if (filtroCostoMin.isNotBlank()) mappaFiltri["costoMin"] = filtroCostoMin.trim()
            if (filtroCostoMax.isNotBlank()) mappaFiltri["costoMax"] = filtroCostoMax.trim()

            // Gestione Date inizio
            if (filtroOrarioInizioMin.isNotBlank()) {
                mappaFiltri["orarioInizioMin"] = "${filtroOrarioInizioMin.trim()}T00:00:00"
            }
            if (filtroOrarioInizioMax.isNotBlank()) {
                mappaFiltri["orarioInizioMax"] = "${filtroOrarioInizioMax.trim()}T23:59:59"
            }

            // Gestione Date fine
            if (filtroOrarioFineMin.isNotBlank()) {
                mappaFiltri["orarioFineMin"] = "${filtroOrarioFineMin.trim()}T00:00:00"
            }
            if (filtroOrarioFineMax.isNotBlank()) {
                mappaFiltri["orarioFineMax"] = "${filtroOrarioFineMax.trim()}T23:59:59"
            }

            try {
                totalePagineAttivita = repository.sincronizzaAttivita(viaggioId, paginaAttivita, mappaFiltri)
                _attivita.value = repository.getAttivitaLocali(viaggioId)
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Impossibile aggiornare il programma delle attività."
            } finally {
                isLoading = false
            }
        }
    }

    fun filtraAttivita(viaggioId: Long) {
        if (isLoading) return
        paginaAttivita = 0
        caricaAttivita(viaggioId)
    }

    fun pulisciFiltriAttivita(viaggioId: Long) {
        if (isLoading) return
        filtroTitolo = ""
        filtroPosizione = ""
        filtroCostoMin = ""
        filtroCostoMax = ""
        filtroOrarioInizioMin = ""
        filtroOrarioInizioMax = ""
        filtroOrarioFineMin = ""
        filtroOrarioFineMax = ""
        paginaAttivita = 0
        caricaAttivita(viaggioId)
    }

    fun cambiaPagina(viaggioId: Long, direzione: Int) {
        val nuovaPagina = paginaAttivita + direzione
        if (nuovaPagina in 0 until totalePagineAttivita) {
            paginaAttivita = nuovaPagina
            caricaAttivita(viaggioId)
        }
    }

    fun aggiungiTappaProgramma(context: Context, viaggioId: Long) {
        if (titoloInput.isBlank() || orarioInizioInput.isBlank() || orarioFineInput.isBlank() || posizioneInput.isBlank()) {
            tipoAvviso = "errore"
            messaggioAvviso = "I campi contrassegnati con * sono obbligatori."
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val costo = costoInput.toDoubleOrNull() ?: 0.0

                val payload = mapOf(
                    "titolo" to titoloInput.trim(),
                    "posizione" to posizioneInput.trim(),
                    "orarioInizio" to orarioInizioInput.trim(),
                    "orarioFine" to orarioFineInput.trim(),
                    "costo" to costo.toString(),
                    "descrizione" to descrizioneInput.trim()
                )

                val res = if (attivitaInModifica && idAttivitaInModifica != null) {
                    api.modificaAttivita(viaggioId, idAttivitaInModifica!!, payload)
                } else {
                    api.creaAttivita(viaggioId, payload)
                }

                if (res.isSuccessful) {
                    pulisciStatoForm()
                    tipoAvviso = "successo"
                    messaggioAvviso = "Attività salvata nel programma!"
                    caricaAttivita(viaggioId)
                }
                else {
                    tipoAvviso = "errore"
                    messaggioAvviso = "Errore dal server"
                }
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Errore di connessione durante il salvataggio."
            } finally { isLoading = false }
        }
    }

    fun avviaModifica(tappa: AttivitaViaggioEntity) {
        attivitaInModifica = true
        idAttivitaInModifica = tappa.id
        titoloInput = tappa.titolo
        posizioneInput = tappa.posizione
        orarioInizioInput = tappa.orarioInizio
        orarioFineInput = tappa.orarioFine
        costoInput = tappa.costo.toString()
        descrizioneInput = tappa.descrizione ?: ""
    }

    fun cancellaTappa(context: Context, viaggioId: Long, idAttivita: Long) {
        if (idAttivitaDaEliminare != idAttivita) {
            idAttivitaDaEliminare = idAttivita
            return
        }
        idAttivitaDaEliminare = null
        viewModelScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val res = api.eliminaAttivita(viaggioId, idAttivita)
                if (res.isSuccessful) {
                    tipoAvviso = "successo"
                    messaggioAvviso = "Attività rimossa con successo."
                    caricaAttivita(viaggioId)
                }
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Impossibile rimuovere la tappa."
            } finally { isLoading = false }
        }
    }

    fun pulisciStatoForm() {
        titoloInput = ""
        posizioneInput = ""
        orarioInizioInput = ""
        orarioFineInput = ""
        costoInput = ""
        descrizioneInput = ""
        attivitaInModifica = false
        idAttivitaInModifica = null
    }
}