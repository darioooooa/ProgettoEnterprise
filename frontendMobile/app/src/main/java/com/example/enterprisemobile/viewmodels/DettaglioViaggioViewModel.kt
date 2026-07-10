package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.ChatActivity // Assicurati che questo import corrisponda alla posizione reale della tua ChatActivity
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.DettaglioViaggioRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.time.LocalDate

class DettaglioViaggioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(application)
    private val repository: DettaglioViaggioRepository

    private var prenotazioneIdAttiva: Long? = null
    var mioUsername by mutableStateOf("")
    var mioRuolo by mutableStateOf("")
    var viaggioId by mutableStateOf(-1L)

    // Stati Generali
    var isLoading by mutableStateOf(false)
    var isEliminazioneInCorso by mutableStateOf(false)
    var messaggioAvviso by mutableStateOf<String?>(null)
    var tipoAvviso by mutableStateOf("errore")
    var mostraDialogEliminazione by mutableStateOf(false)

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
                        prenotazioneIdAttiva = infoPrenotazione.id
                        statoSvolgimentoIscrizione = calcolaSvolgimentoReale(viaggioEntity)
                    } else {
                        isGiaAcquistato = false
                        prenotazioneIdAttiva = null
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

        val v = viaggioEntity ?: return
        val s = statisticheDto

        viewModelScope.launch {
            isLoading = true
            try {
                val payloadDto = ViaggioDTO(
                    id = v.id,
                    titolo = v.titolo,
                    descrizione = v.descrizione,
                    stato = "APERTO",
                    maxPartecipanti = v.maxPartecipanti,
                    partecipantiAttuali = v.partecipantiAttuali,
                    destinazione = v.destinazione,
                    cittaPartenza = v.cittaPartenza,
                    prezzo = prezzoDouble, // Il nuovo prezzo numerico modificato
                    dataInizio = v.dataInizio,
                    dataFine = v.dataFine,
                    latitudine = 0.0,
                    longitudine = 0.0,
                    organizzatoreId = s?.organizzatoreId,
                    organizzatoreUsername = s?.organizzatoreUsername
                )
                val response = repository.modificaViaggio(viaggioId, payloadDto)

                if (response.isSuccessful) {
                    inModificaPrezzo = false

                    val entityAggiornata = v.copy(prezzo = prezzoDouble)
                    repository.salvaViaggioLocale(entityAggiornata)
                    viaggioEntity = entityAggiornata

                    tipoAvviso = "successo"
                    messaggioAvviso = "Prezzo aggiornato con successo!"

                    caricaDatiCompleti()
                } else {
                    tipoAvviso = "errore"
                    messaggioAvviso = "Errore durante la modifica."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tipoAvviso = "errore"
                messaggioAvviso = "Impossibile aggiornare il prezzo."
            } finally {
                isLoading = false
            }
        }
    }

    fun eliminaViaggioCorrente(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isEliminazioneInCorso = true
            mostraDialogEliminazione = false
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
        val idPrenotazione = prenotazioneIdAttiva
        if (idPrenotazione == null) {
            tipoAvviso = "errore"
            messaggioAvviso = "Impossibile trovare una prenotazione valida per esportare il calendario."
            return
        }

        viewModelScope.launch {
            isLoading = true
            messaggioAvviso = null

            try {
                val api = RetrofitClient.ottieniPrenotazioneService(getApplication())
                val response = api.scaricaFileIcs(idPrenotazione)

                if (response.isSuccessful && response.body() != null) {
                    val nomeFile = "prenotazione_$idPrenotazione.ics"

                    // Scrittura asincrona del file su IO thread
                    val successoSalvataggio = withContext(Dispatchers.IO) {
                        salvaFileIcsSuDispositivo(response.body()!!, nomeFile)
                    }

                    if (successoSalvataggio) {
                        tipoAvviso = "successo"
                        messaggioAvviso = "📅 Calendario (.ics) salvato nei tuoi download!"
                    } else {
                        throw Exception("Errore durante la scrittura del file.")
                    }
                } else {
                    throw Exception("Risposta del server non valida.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tipoAvviso = "errore"
                messaggioAvviso = "Errore durante il download del calendario: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Funzione per scrivere il file usando il MediaStore
    private fun salvaFileIcsSuDispositivo(body: ResponseBody, nomeFile: String): Boolean {
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver

        val dettagliFile = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nomeFile)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/calendar")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
            }
        }

        val uriRaccolta = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            // Fallback per vecchie versioni di Android
            MediaStore.Files.getContentUri("external")
        }

        val fileUri = contentResolver.insert(uriRaccolta, dettagliFile) ?: return false

        return try {
            contentResolver.openOutputStream(fileUri).use { outputStream ->
                if (outputStream == null) return false
                body.byteStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Rimuove il file vuoto/corrotto se la scrittura fallisce
            contentResolver.delete(fileUri, null, null)
            false
        }
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

    fun avviaConversazioneConOrganizzatore(contestoDiEsecuzione: android.content.Context) {
        viewModelScope.launch {
            try {
                val interfacciaChat = RetrofitClient.ottieniChatService(getApplication())

                val identificativoStanza = interfacciaChat.creaOApriStanzaPrivata(
                    identificativoViaggio = viaggioId,
                    nomeUtenteViaggiatore = mioUsername
                )

                val intentoPerLaChat = Intent(contestoDiEsecuzione, ChatActivity::class.java).apply {
                    putExtra("ID_STANZA", identificativoStanza)
                }
                contestoDiEsecuzione.startActivity(intentoPerLaChat)

            } catch (eccezioneDiRete: Exception) {
                eccezioneDiRete.printStackTrace()
                Toast.makeText(contestoDiEsecuzione, "Impossibile aprire la conversazione in questo momento", Toast.LENGTH_SHORT).show()
            }
        }
    }
}