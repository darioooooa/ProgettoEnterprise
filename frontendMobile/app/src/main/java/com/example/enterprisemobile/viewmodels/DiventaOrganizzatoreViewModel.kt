package com.example.enterprisemobile.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.model.RichiestaPromozioneDTO
import com.example.enterprisemobile.data.security.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class DiventaOrganizzatoreViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.ottieniViaggiatoreService(application)

    var usernameRichiesto by mutableStateOf("")
    var emailProfessionale by mutableStateOf("")
    var motivazione by mutableStateOf("")
    var biografiaProfessionale by mutableStateOf("")

    // Memorizziamo sia il nome (per la grafica) sia l'Uri (per inviare il file)
    var nomeFileSelezionato by mutableStateOf("Nessun file selezionato")
    var uriFileSelezionato by mutableStateOf<Uri?>(null)

    var isLoading by mutableStateOf(false)
    var messaggioErrore by mutableStateOf<String?>(null)
    var mostraModaleInSospeso by mutableStateOf(false)
    var richiestaInviataConSuccesso by mutableStateOf(false)
    var mostraModaleConferma by mutableStateOf(false)


    fun inviaRichiesta(context: Context) {

        if (usernameRichiesto.isBlank() || emailProfessionale.isBlank() || motivazione.isBlank() || biografiaProfessionale.isBlank()) {
            messaggioErrore = "Compila tutti i campi obbligatori."
            return
        }


        if (uriFileSelezionato == null) {
            messaggioErrore = "È obbligatorio allegare un documento (CV o Portfolio)."
            return
        }

        viewModelScope.launch {
            isLoading = true
            messaggioErrore = null
            try {
                // Prepariamo i dati testuali in JSON
                val richiestaDto = RichiestaPromozioneDTO(
                    usernameRichiesto = usernameRichiesto,
                    emailProfessionale = emailProfessionale,
                    motivazione = motivazione,
                    biografiaProfessionale = biografiaProfessionale,
                    documentiLink = nomeFileSelezionato
                )

                val gson = Gson()
                val jsonRichiesta = gson.toJson(richiestaDto)
                val richiestaBody = jsonRichiesta.toRequestBody("application/json".toMediaTypeOrNull())

                // Prepariamo il file fisico
                var filePart: MultipartBody.Part? = null

                if (uriFileSelezionato != null) {
                    val tempFile = File(context.cacheDir, nomeFileSelezionato ?: "documento.pdf")
                    context.contentResolver.openInputStream(uriFileSelezionato!!)?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val estensione = tempFile.extension.lowercase()
                    val tipoFile = when (estensione) {
                        "pdf" -> "application/pdf"
                        "doc" -> "application/msword"
                        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        else -> context.contentResolver.getType(uriFileSelezionato!!) ?: "application/octet-stream"
                    }

                    val requestFile = tempFile.asRequestBody(tipoFile.toMediaTypeOrNull())
                    filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                }

                // Invio al server
                val response = apiService.inviaRichiestaPromozione(richiestaBody, filePart)

                // Verifica della risposta
                if (response.isSuccessful) {
                    richiestaInviataConSuccesso = true
                } else {
                    val codiceErrore = response.code()
                    val corpoErrore = response.errorBody()?.string()

                    android.util.Log.e("CANDIDATURA_ERROR", "Errore $codiceErrore dal server: $corpoErrore")

                    // --- LA CORREZIONE È QUI: Separiamo i messaggi di errore ---
                    when (codiceErrore) {
                        409 -> mostraModaleInSospeso = true // Conflitto: c'è già una richiesta
                        400 -> messaggioErrore = "Formato file non supportato. Assicurati che sia un PDF, DOC o DOCX."
                        401, 403 -> messaggioErrore = "Errore di autenticazione o permessi negati."
                        else -> messaggioErrore = "Impossibile inviare la candidatura. Errore server ($codiceErrore)."
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("CANDIDATURA_ERROR", "Eccezione di rete: ${e.message}")
                messaggioErrore = "Errore di connessione al server."
            } finally {
                isLoading = false
            }
        }
    }

    fun chiudiModale() {
        mostraModaleInSospeso = false
    }
}