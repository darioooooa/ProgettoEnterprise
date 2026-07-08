package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.ViaggioEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.enterprisemobile.model.ViaggioDTO

class ViaggioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ViaggioRepository
    val viaggiSalvati: StateFlow<List<ViaggioEntity>> 

    var nomeUtente by mutableStateOf("Caricamento...")
    var paginaCorrente by mutableIntStateOf(0)
    var totalePagine by mutableIntStateOf(1)
    var ricercaEffettuata by mutableStateOf(false)

    var viaggiConsigliati by mutableStateOf<List<ViaggioDTO>>(emptyList())
    var isLoadingConsigliati by mutableStateOf(false)
    var erroreConsigliati by mutableStateOf<String?>(null)
    init {
        val apiService = RetrofitClient.ottieniViaggioService(application)
        val dao = AppDatabase.getInstance(application).viaggioDao()
        repository = ViaggioRepository(apiService, dao)

        val sessionManager = SessionManager(application)
        nomeUtente = sessionManager.ottieniUsername() ?: "Utente"

        viaggiSalvati = repository.allViaggi.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun cercaViaggi(destinazione: String, dataMin: String, dataMax: String, posti: String, pMin: String, pMax: String, pagina: Int) {
        viewModelScope.launch {
            try {
                val numPagineRicevute = repository.refreshViaggiFiltrati(destinazione, dataMin, dataMax, posti, pMin, pMax, pagina)

                if (numPagineRicevute > 0) {
                    totalePagine = numPagineRicevute
                }

                ricercaEffettuata = true
                paginaCorrente = pagina
            } catch (e: Exception) {
                android.util.Log.e("RETE", "Errore: ${e.message}")
            }
        }
    }

    fun caricaViaggiConsigliati() {
        viewModelScope.launch {
            isLoadingConsigliati = true
            erroreConsigliati = null
            try {
                val apiService = RetrofitClient.ottieniViaggioService(getApplication())
                val response = apiService.getViaggiConsigliati()
                if (response.isSuccessful && response.body() != null) {
                    viaggiConsigliati = response.body()!!
                } else {
                    erroreConsigliati = "Errore nel caricamento dei consigli"
                }
            } catch (e: Exception) {
                erroreConsigliati = e.message
                android.util.Log.e("CONSIGLIATI", "Errore: ${e.message}")
            } finally {
                isLoadingConsigliati = false
            }
        }
    }
}