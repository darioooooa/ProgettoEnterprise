package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.GalleriaRepository
import com.example.enterprisemobile.model.ImmagineViaggioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleriaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GalleriaRepository

    private val _immagini = MutableStateFlow<List<ImmagineViaggioEntity>>(emptyList())
    val immagini: StateFlow<List<ImmagineViaggioEntity>> = _immagini

    var immagineCorrenteIndex by mutableIntStateOf(0)
    var isLoading by mutableStateOf(false)
    var messaggioAvviso by mutableStateOf<String?>(null)
    var tipoAvviso by mutableStateOf("errore")

    // Input form organizzatore
    var nuovaImmagineUrl by mutableStateOf("")
    var nuovaImmaginePubblica by mutableStateOf(true)
    var idImmagineDaEliminare by mutableStateOf<Long?>(null)

    init {
        val api = RetrofitClient.ottieniViaggioService(application)
        val dao = AppDatabase.getInstance(application).galleriaDao()
        repository = GalleriaRepository(api, dao)
    }

    fun caricaImmagini(viaggioId: Long) {
        viewModelScope.launch {
            isLoading = true
            repository.sincronizzaGalleria(viaggioId)
            _immagini.value = repository.getImmaginiLocali(viaggioId)
            isLoading = false
        }
    }

    fun aggiungiImmagine(context: Context, viaggioId: Long) {
        if (nuovaImmagineUrl.isBlank()) {
            tipoAvviso = "errore"
            messaggioAvviso = "Inserire un URL valido per l'immagine."
            return
        }
        viewModelScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val res = api.aggiungiImmagine(viaggioId, nuovaImmagineUrl.trim(), nuovaImmaginePubblica)
                if (res.isSuccessful) {
                    nuovaImmagineUrl = ""
                    repository.sincronizzaGalleria(viaggioId)
                    val listaAggiornata = repository.getImmaginiLocali(viaggioId)
                    _immagini.value = listaAggiornata
                    if (listaAggiornata.isNotEmpty()) immagineCorrenteIndex = listaAggiornata.size - 1
                    tipoAvviso = "successo"
                    messaggioAvviso = "Immagine aggiunta alla galleria!"
                }
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Errore di connessione."
            } finally { isLoading = false }
        }
    }

    fun cambiaVisibilita(context: Context, viaggioId: Long, img: ImmagineViaggioEntity) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val res = api.modificaVisibilita(viaggioId, img.id, !img.pubblica)
                if (res.isSuccessful) {
                    repository.sincronizzaGalleria(viaggioId)
                    _immagini.value = repository.getImmaginiLocali(viaggioId)
                }
            } catch (e: Exception) { }
        }
    }

    fun cancellaImmagine(context: Context, viaggioId: Long, idImmagine: Long) {
        if (idImmagineDaEliminare != idImmagine) {
            idImmagineDaEliminare = idImmagine
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                if (idImmagineDaEliminare == idImmagine) {
                    idImmagineDaEliminare = null
                }
            }
            return
        }
        idImmagineDaEliminare = null
        viewModelScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.ottieniViaggioService(context)
                val res = api.eliminaImmagine(viaggioId, idImmagine)
                if (res.isSuccessful) {
                    repository.sincronizzaGalleria(viaggioId)
                    val listaAggiornata = repository.getImmaginiLocali(viaggioId)
                    _immagini.value = listaAggiornata
                    if (immagineCorrenteIndex >= listaAggiornata.size) {
                        immagineCorrenteIndex = maxOf(0, listaAggiornata.size - 1)
                    }
                    tipoAvviso = "successo"
                    messaggioAvviso = "Immagine rimossa dal viaggio."
                }
            } catch (e: Exception) {
                tipoAvviso = "errore"
                messaggioAvviso = "Errore eliminazione."
            }
            finally { isLoading = false }
        }
    }
}