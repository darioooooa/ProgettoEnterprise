package com.example.enterprisemobile.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.model.PagamentoDTO
import com.example.enterprisemobile.data.security.SessionManager
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch

class PagamentoViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.ottieniPagamentoService(application)
    val nomeUtente = SessionManager(application).ottieniUsername() ?: "Utente"

    var isCaricamento by mutableStateOf(false)
    var messaggioErrore by mutableStateOf<String?>(null)
    var pagamentoCompletato by mutableStateOf(false)

    var clientSecret by mutableStateOf<String?>(null)
    var nomeTitolare by mutableStateOf("")
    fun preparaPagamento(idPrenotazione: Long) {
        viewModelScope.launch {
            isCaricamento = true
            try {
                val risposta = apiService.creaPaymentIntent(idPrenotazione)
                clientSecret = risposta["clientSecret"]
            } catch (e: Exception) {
                messaggioErrore = "Impossibile contattare il server per il pagamento."
            } finally {
                isCaricamento = false
            }
        }
    }

    fun gestisciRisultatoStripe(risultato: PaymentSheetResult, idPrenotazione: Long, importoDaPagare: Double) {
        when (risultato) {
            is PaymentSheetResult.Completed -> {
                confermaPagamentoAlBackend(idPrenotazione, importoDaPagare)
            }
            is PaymentSheetResult.Canceled -> {
                messaggioErrore = "Pagamento annullato dall'utente."
            }
            is PaymentSheetResult.Failed -> {
                messaggioErrore = "Errore durante il pagamento: ${risultato.error.message}"
            }
        }
    }

    private fun confermaPagamentoAlBackend(idPrenotazione: Long, importo: Double) {
        viewModelScope.launch {
            isCaricamento = true
            try {
                val ricevuta = PagamentoDTO(
                    idPrenotazione = idPrenotazione,
                    importo = importo,
                    ricevutaPagamento = clientSecret ?: "token_sconosciuto",
                    titolareCarta = nomeTitolare
                )
                apiService.confermaPagamento(ricevuta)
                pagamentoCompletato = true
            } catch (e: Exception) {
                messaggioErrore = "Errore durante la conferma della ricevuta."
            } finally {
                isCaricamento = false
            }
        }
    }
}