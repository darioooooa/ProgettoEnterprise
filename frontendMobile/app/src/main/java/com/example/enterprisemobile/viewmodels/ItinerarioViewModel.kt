package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.ItinerarioRepository
import com.example.enterprisemobile.model.InvitoSospesoDTO
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import com.example.enterprisemobile.model.Visibilita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItinerarioViewModel(application: Application) : AndroidViewModel(application) {

    private val service = RetrofitClient.ottieniItinerariService(application)
    private val repository = ItinerarioRepository(service, AppDatabase.getInstance(application).itinerarioDao())

    private val _itinerari = MutableStateFlow<List<ItinerarioPreferitoDTO>>(emptyList())
    val itinerari: StateFlow<List<ItinerarioPreferitoDTO>> = _itinerari.asStateFlow()

    private val _itinerariCondivisi = MutableStateFlow<List<ItinerarioPreferitoDTO>>(emptyList())
    val itinerariCondivisi: StateFlow<List<ItinerarioPreferitoDTO>> = _itinerariCondivisi.asStateFlow()

    private val _invitiInSospeso = MutableStateFlow<List<InvitoSospesoDTO>>(emptyList())
    val invitiInSospeso: StateFlow<List<InvitoSospesoDTO>> = _invitiInSospeso.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val menuSpostaAperto = mutableStateMapOf<String, Boolean>()

    // Carica itinerari e inviti (mie Liste + condivisi + pendenti)
    fun caricaItinerari() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _itinerari.value = repository.getMieiItinerari()
                _itinerariCondivisi.value = repository.getItinerariCondivisi()
                _invitiInSospeso.value = repository.getInvitiInSospeso()

                // Svuota i vecchi stati dei menu aperti per non lasciare tendine orfane
                menuSpostaAperto.clear()
            } catch (e: Exception) {
                _itinerari.value = repository.getMieiItinerari()
                _itinerariCondivisi.value = repository.getItinerariCondivisi()
                _errorMessage.value = "Impossibile aggiornare i dati. Cache locale attiva."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Accetta un invito di collaborazione
    fun accettaInvitoCollaborazione(idItinerario: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.accettaInvito(idItinerario)
                if (res.isSuccessful) {
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Impossibile accettare l'invito dal server."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore di connessione durante l'accettazione."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Rifiuta un invito di collaborazione
    fun rifiutaInvitoCollaborazione(idItinerario: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.rifiutaInvito(idItinerario)
                if (res.isSuccessful) {
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Impossibile rifiutare l'invito dal server."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore di connessione durante il rifiuto."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crea un nuovo itinerario vuoto
    fun creaItinerario(nomeItinerario: String, visibilitaScelta: Visibilita) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.creaItinerario(nomeItinerario, visibilitaScelta)
                if (res.isSuccessful) {
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Errore durante la creazione sul server."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore di connessione."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cambia la visibilità (PRIVATA / PUBBLICA)
    fun cambiaVisibilitaItinerario(id: Long, visibilitaAttuale: Visibilita) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.cambiaVisibilita(id, visibilitaAttuale)
                if (res.isSuccessful) {
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Impossibile cambiare la visibilità sul server."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore di rete durante la modifica."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Elimina un itinerario
    fun avviaEliminazioneItinerario(idDaCancellare: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.eliminaItinerario(idDaCancellare)
                if (res.isSuccessful) {
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Impossibile eliminare l'itinerario."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore di connessione durante la rimozione."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Aggiunge un viaggio a un itinerario esistente
    fun aggiungiViaggioAItinerario(idLista: Long, idViaggio: Long, onEsito: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.aggiungiViaggioAItinerario(idLista, idViaggio)
                if (res.isSuccessful) {
                    caricaItinerari()
                    onEsito(true)
                } else {
                    onEsito(false)
                }
            } catch (e: Exception) {
                onEsito(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Rimuove un viaggio da un itinerario
    fun rimuoviViaggioDaLista(idLista: Long, idViaggio: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.rimuoviViaggioDaItinerario(idLista, idViaggio)
                if (res.isSuccessful) {
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Impossibile rimuovere il viaggio dal server."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore di rete durante la rimozione del viaggio."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Sposta un viaggio da un itinerario ad un altro
    fun spostaViaggioItinerario(idSorgente: Long, idDestinazione: Long, idViaggio: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.spostaViaggioTraItinerari(idSorgente, idDestinazione, idViaggio)
                if (res.isSuccessful) {
                    menuSpostaAperto["$idSorgente-$idViaggio"] = false
                    caricaItinerari()
                } else {
                    _errorMessage.value = "Impossibile spostare il viaggio."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore durante lo spostamento di rete."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Invita un collaboratore in un itinerario personale
    fun invitaCollaboratoreInItinerario(idLista: Long, email: String, onCompletato: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.invitaCollaboratore(idLista, email)
                if (res.isSuccessful) {
                    onCompletato(true, "Invito inviato con successo a $email")
                } else {
                    onCompletato(false, "Impossibile invitare questo utente. Verifica l'email.")
                }
            } catch (e: Exception) {
                onCompletato(false, "Errore di connessione di rete.")
            } finally {
                _isLoading.value = false
                caricaItinerari()
            }
        }
    }
}