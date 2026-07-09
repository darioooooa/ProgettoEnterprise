package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.AdminRepository
import com.example.enterprisemobile.data.repository.CommunityRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.RecensioneViaggioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CommunityRepository
    private val sessionManager = SessionManager(application)

    private val _recensioni = MutableStateFlow<List<RecensioneViaggioEntity>>(emptyList())
    val recensioni: StateFlow<List<RecensioneViaggioEntity>> = _recensioni

    var mioUsername by mutableStateOf("")
    var mioRuolo by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var messaggioAvviso by mutableStateOf<String?>(null)
    var tipoAvviso by mutableStateOf("errore")
    var paginaRecensioni by mutableIntStateOf(0)
    var totalePagineRecensioni by mutableIntStateOf(1)

    // Form Recensione
    var votoInput by mutableIntStateOf(5)
    var commentoInput by mutableStateOf("")
    var inModifica by mutableStateOf(false)
    var idRecensioneInModifica by mutableStateOf<Long?>(null)
    var idRecensioneDaEliminare by mutableStateOf<Long?>(null)
    var laMiaRecensione by mutableStateOf<RecensioneViaggioEntity?>(null)

    var haGiaRecensito by mutableStateOf(false)

    // Variabili per i filtri
    var filtroParolaChiave by mutableStateOf("")
    var filtroVotoEsatto by mutableStateOf("")
    var filtroDataDa by mutableStateOf("")
    var filtroDataA by mutableStateOf("")

    // ✅ NUOVI: Stati per segnalazione recensione
    var showSegnalazioneRecensioneDialog by mutableStateOf(false)
    var recensioneDaSegnalare by mutableStateOf<RecensioneViaggioEntity?>(null)
    var isLoadingSegnalazione by mutableStateOf(false)

    init {
        mioUsername = sessionManager.ottieniUsername() ?: ""
        mioRuolo = sessionManager.ottieniRuolo() ?: ""
        val api = RetrofitClient.ottieniViaggioService(application)
        val dao = AppDatabase.getInstance(application).communityDao()
        repository = CommunityRepository(api, dao)
    }

    fun caricaRecensioni(viaggioId: Long) {
        viewModelScope.launch {
            isLoading = true
            _recensioni.value = emptyList()

            val mappaFiltri = mutableMapOf<String, String>()

            if (filtroParolaChiave.isNotBlank()) {
                mappaFiltri["parolaChiave"] = filtroParolaChiave.trim()
            }

            if (filtroVotoEsatto.isNotBlank()) {
                mappaFiltri["votoMin"] = filtroVotoEsatto
                mappaFiltri["votoMax"] = filtroVotoEsatto
            }

            if (filtroDataDa.isNotBlank()) {
                mappaFiltri["dataDa"] = "${filtroDataDa}T00:00:00"
            }
            if (filtroDataA.isNotBlank()) {
                mappaFiltri["dataA"] = "${filtroDataA}T23:59:59"
            }

            totalePagineRecensioni = repository.sincronizzaRecensioni(viaggioId, paginaRecensioni, mappaFiltri)

            val locali = repository.getRecensioniLocali(viaggioId)
            _recensioni.value = locali

            val recensioneUtente = locali.find { it.utenteUsername == mioUsername }

            if (recensioneUtente != null) {
                laMiaRecensione = recensioneUtente
                haGiaRecensito = true
            } else if (mappaFiltri.isEmpty() && paginaRecensioni == 0) {
                laMiaRecensione = null
                haGiaRecensito = false
            }
            isLoading = false
        }
    }

    fun filtraRecensioni(viaggioId: Long) {
        if (isLoading) return
        paginaRecensioni = 0
        caricaRecensioni(viaggioId)
    }

    fun pulisciFiltriRecensioni(viaggioId: Long) {
        if (isLoading) return
        filtroParolaChiave = ""
        filtroVotoEsatto = ""
        filtroDataDa = ""
        filtroDataA = ""
        paginaRecensioni = 0
        caricaRecensioni(viaggioId)
    }

    fun cambiaPagina(viaggioId: Long, direzione: Int) {
        val nuova = paginaRecensioni + direzione
        if (nuova in 0 until totalePagineRecensioni) {
            paginaRecensioni = nuova
            caricaRecensioni(viaggioId)
        }
    }

    fun aggiungiRecensione(context: Context, viaggioId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val payload = mapOf("voto" to votoInput.toString(), "commento" to commentoInput.trim())

                val res = if (inModifica && idRecensioneInModifica != null) {
                    api.aggiornaRecensione(viaggioId, idRecensioneInModifica!!, payload)
                } else {
                    api.inviaRecensione(viaggioId, payload)
                }

                if (res.isSuccessful) {
                    pulisciForm()
                    tipoAvviso = "successo"
                    messaggioAvviso = "Recensione salvata con successo!"
                    paginaRecensioni = 0
                    filtroParolaChiave = ""
                    filtroVotoEsatto = ""
                    filtroDataDa = ""
                    filtroDataA = ""
                    caricaRecensioni(viaggioId)
                }
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Errore nel salvataggio."
            } finally { isLoading = false }
        }
    }

    fun avviaModifica(rec: RecensioneViaggioEntity) {
        inModifica = true
        idRecensioneInModifica = rec.id
        votoInput = rec.voto
        commentoInput = rec.commento ?: ""
    }

    fun cancellaRecensione(context: Context, viaggioId: Long, idRecensione: Long) {
        if (idRecensioneDaEliminare != idRecensione) {
            idRecensioneDaEliminare = idRecensione
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                if (idRecensioneDaEliminare == idRecensione) {
                    idRecensioneDaEliminare = null
                }
            }
            return
        }
        idRecensioneDaEliminare = null
        viewModelScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val res = api.eliminaRecensione(viaggioId, idRecensione)
                if (res.isSuccessful) {
                    val eraLaMia = laMiaRecensione?.id == idRecensione
                    if (eraLaMia) {
                        haGiaRecensito = false
                        laMiaRecensione = null
                    }
                    pulisciForm()
                    tipoAvviso = "successo"
                    messaggioAvviso = "Recensione rimossa."
                    caricaRecensioni(viaggioId)
                }
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Errore eliminazione."
            }
            finally { isLoading = false }
        }
    }

    fun pulisciForm() {
        votoInput = 5
        commentoInput = ""
        inModifica = false
        idRecensioneInModifica = null
    }


    fun apriDialogSegnalazioneRecensione(recensione: RecensioneViaggioEntity) {
        recensioneDaSegnalare = recensione
        showSegnalazioneRecensioneDialog = true
    }

    fun chiudiDialogSegnalazioneRecensione() {
        showSegnalazioneRecensioneDialog = false
        recensioneDaSegnalare = null
    }

    fun inviaSegnalazioneRecensione(
        context: Context,
        motivo: String,
        descrizione: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoadingSegnalazione = true
            try {
                val recensione = recensioneDaSegnalare ?: return@launch
                val adminRepo = AdminRepository(context)

                val response = adminRepo.creaSegnalazione(
                    tipo = "RECENSIONE",
                    idRiferimento = recensione.id,
                    motivo = motivo,
                    descrizione = descrizione,
                    idSegnalatore = sessionManager.ottieniIdUtente()?.toLongOrNull() ?: 0L
                )

                if (response.isSuccessful) {
                    onSuccess()
                    chiudiDialogSegnalazioneRecensione()
                } else {
                    val errorJson = response.errorBody()?.string()
                    val messaggioAvviso = try {
                        org.json.JSONObject(errorJson ?: "").getString("messaggio")
                    } catch (e: Exception) {
                        "Errore nell'invio della segnalazione"
                    }
                    onError(messaggioAvviso)
                }
            } catch (e: Exception) {
                onError("Errore: ${e.message}")
            } finally {
                isLoadingSegnalazione = false
            }
        }
    }
}