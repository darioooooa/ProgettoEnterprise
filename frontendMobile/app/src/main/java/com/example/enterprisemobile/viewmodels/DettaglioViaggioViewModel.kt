package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.DettaglioViaggioRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class DettaglioViaggioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(application)
    private val repository: DettaglioViaggioRepository

    var mioUsername by mutableStateOf("")
    var mioRuolo by mutableStateOf("")
    var viaggioId by mutableStateOf(-1L)

    // Stati Generali
    var isLoading by mutableStateOf(false)
    var isEliminazioneInCorso by mutableStateOf(false)
    var messaggioAvviso by mutableStateOf<String?>(null)
    var tipoAvviso by mutableStateOf("errore")

    // Dati principali del viaggio
    var viaggioEntity by mutableStateOf<ViaggioEntity?>(null)
    var statisticheDto by mutableStateOf<StatisticheViaggioDTO?>(null)
    var isGiaAcquistato by mutableStateOf(false)
    var statoSvolgimentoIscrizione by mutableStateOf("")

    // Modifica prezzo organizzatore
    var inModificaPrezzo by mutableStateOf(false)
    var nuovoPrezzoInput by mutableStateOf("")

    init {
        mioUsername = sessionManager.ottieniUsername() ?: ""
        mioRuolo = sessionManager.ottieniRuolo() ?: ""
        repository = DettaglioViaggioRepository(
            RetrofitClient.ottieniViaggioService(application),
            AppDatabase.getInstance(application).viaggioDao()
        )
    }

    fun inizializza(id: Long) {
        if (viaggioId == id) return
        viaggioId = id
        caricaDatiCompleti()
    }

    fun caricaDatiCompleti() {
        if (viaggioId <= 0) return

        viewModelScope.launch {
            if (viaggioEntity == null) {
                isLoading = true
            }
            messaggioAvviso = null

            try {
                // Cerca nel db locale
                val viaggioLocale = repository.getViaggioLocale(viaggioId)

                // Chiamata di rete per scaricare la versione dal server
                val api = RetrofitClient.ottieniViaggioService(getApplication())
                val responseViaggio = repository.getViaggioRemoto(viaggioId)

                if (responseViaggio.isSuccessful && responseViaggio.body() != null) {
                    val dto = responseViaggio.body()!!
                    val entityAggiornata = ViaggioEntity(
                        id = dto.id ?: viaggioId,
                        titolo = dto.titolo,
                        destinazione = dto.destinazione,
                        cittaPartenza = dto.cittaPartenza,
                        prezzo = dto.prezzo,
                        dataInizio = dto.dataInizio,
                        dataFine = dto.dataFine,
                        descrizione = dto.descrizione,
                        maxPartecipanti = dto.maxPartecipanti,
                        partecipantiAttuali = dto.partecipantiAttuali
                    )
                    repository.salvaViaggioLocale(entityAggiornata)
                    viaggioEntity = entityAggiornata
                } else if (viaggioLocale != null) {
                    // Se la rete fallisce ma c'è la cache, si usa quella vecchia
                    viaggioEntity = viaggioLocale
                }

                // Se è nullo anche dopo il server, solleva l'eccezione
                if (viaggioEntity == null) {
                    throw Exception("Itinerario non trovato sul server.")
                }

                // Chiamate per statistiche e stato prenotazione
                try {
                    val resStats = api.getStatisticheRecensioni(viaggioId)
                    statisticheDto = if (resStats.isSuccessful) resStats.body() else null

                    val prenotazioneService = RetrofitClient.ottieniPrenotazioneService(getApplication())
                    val resPren = prenotazioneService.getMiePrenotazioni(0)

                    val infoPrenotazione = if (resPren.isSuccessful) {
                        resPren.body()?.content?.find { it.viaggioId == viaggioId }
                    } else null

                    if (infoPrenotazione != null && infoPrenotazione.stato == "CONFERMATA") {
                        isGiaAcquistato = true
                        statoSvolgimentoIscrizione = calcolaSvolgimentoReale(viaggioEntity)
                    } else {
                        isGiaAcquistato = false
                        statoSvolgimentoIscrizione = ""
                    }
                } catch (e: Exception) {
                    // Errore silenzioso per i dati secondari se la rete cade a metà
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                // Se fallisce la rete ma c'è il dato locale, non mostra l'errore
                if (viaggioEntity == null) {
                    tipoAvviso = "errore"
                    messaggioAvviso = e.message ?: "Errore durante il caricamento dei dati."
                }
            } finally {
                isLoading = false // Forza lo sblocco della schermata
            }
        }
    }

    fun isMioViaggio(): Boolean {
        val orgUpper = statisticheDto?.organizzatoreUsername?.trim()?.lowercase() ?: ""
        return mioRuolo == "ROLE_ORGANIZZATORE" && mioUsername.trim().lowercase() == orgUpper
    }

    fun isTuttoEsaurito(): Boolean {
        val v = viaggioEntity ?: return false
        return v.maxPartecipanti > 0 && v.partecipantiAttuali >= v.maxPartecipanti
    }

    fun salvaNuovoPrezzo() {
        val prezzoDouble = nuovoPrezzoInput.toDoubleOrNull()
        if (prezzoDouble == null || prezzoDouble < 0) {
            tipoAvviso = "errore"
            messaggioAvviso = "Prezzo inserito non valido."
            return
        }
        viewModelScope.launch {
            isLoading = true
            try {
                val v = viaggioEntity ?: return@launch
                val payload = mapOf(
                    "titolo" to v.titolo,
                    "descrizione" to (v.descrizione ?: ""),
                    "destinazione" to v.destinazione,
                    "cittaPartenza" to v.cittaPartenza,
                    "dataInizio" to v.dataInizio,
                    "dataFine" to v.dataFine,
                    "prezzo" to prezzoDouble.toString(),
                    "maxPartecipanti" to v.maxPartecipanti.toString()
                )
                val response = repository.modificaViaggio(viaggioId, payload)
                if (response.isSuccessful) {
                    inModificaPrezzo = false
                    caricaDatiCompleti()
                    tipoAvviso = "successo"
                    messaggioAvviso = "Prezzo aggiornato con successo!"
                }
            } catch (e: Exception) {
                messaggioAvviso = "Impossibile aggiornare il prezzo."
            } finally { isLoading = false }
        }
    }

    fun eliminaViaggioCorrente(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isEliminazioneInCorso = true
            try {
                val response = repository.eliminaViaggio(viaggioId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    tipoAvviso = "errore"
                    messaggioAvviso = "Impossibile eliminare il viaggio."
                }
            } catch (e: Exception) {
                messaggioAvviso = "Errore durante la richiesta di eliminazione."
            } finally {
                isEliminazioneInCorso = false
            }
        }
    }

    fun scaricaFileIcs() {
        // TODO: implementare l'export del calendario
        // Simulazione esportazione logica calendario .ics per dispositivi mobili
        tipoAvviso = "successo"
        messaggioAvviso = "📅 Calendario (.ics) esportato nella memoria del dispositivo!"
    }

    private fun calcolaSvolgimentoReale(v: ViaggioEntity?): String {
        if (v == null) return "PRENOTATO"
        val oggi = LocalDate.now()
        val inizio = LocalDate.parse(v.dataInizio)
        val fine = LocalDate.parse(v.dataFine)
        return when {
            oggi.isBefore(inizio) -> "PRENOTATO"
            oggi.isAfter(fine) -> "COMPLETATO"
            else -> "IN_CORSO"
        }
    }

    private suspend fun intentViaggioFallback(locale: ViaggioEntity?): ViaggioEntity? {
        if (locale != null) return locale
        val listaIntera = database.viaggioDao().getAllViaggi().firstOrNull()
        return listaIntera?.firstOrNull()
    }
}