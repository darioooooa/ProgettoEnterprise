package com.example.enterprisemobile.viewmodels

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// Rappresenta i possibili stati della schermata di login
sealed class StatoAuth {
    object Iniziale : StatoAuth()
    object Caricamento : StatoAuth()
    data class Successo(val ruolo: String) : StatoAuth()
    data class Errore(val messaggio: String) : StatoAuth()
}

class AuthViewModel : ViewModel() {

    private val _statoUi = MutableStateFlow<StatoAuth>(StatoAuth.Iniziale)
    val statoUi: StateFlow<StatoAuth> = _statoUi.asStateFlow()

    // Esegue il tentativo di accesso parlando con Keycloak e salvando la sessione
    fun eseguiLogin(context: Context, usernameInput: String, passwordInput: String) {
        if (usernameInput.isBlank() || passwordInput.isBlank()) {
            _statoUi.value = StatoAuth.Errore("Username e password non possono essere vuoti.")
            return
        }

        _statoUi.value = StatoAuth.Caricamento

        // Avvia l'operazione asincrona in un thread in background (Coroutine)
        viewModelScope.launch {
            try {
                val sessionManager = SessionManager(context)
                val authService = RetrofitClient.ottieniAuthService(context)

                // Costruisce i parametri x-www-form-urlencoded richiesti da Keycloak
                val parametri = mapOf(
                    "grant_type" to "password",
                    "client_id" to "enterprise-client",
                    "username" to usernameInput,
                    "password" to passwordInput
                )

                val risposta = authService.effettuaAccesso(parametri)

                if (risposta.isSuccessful && risposta.body() != null) {
                    val datiToken = risposta.body()!!

                    // Salva i token
                    sessionManager.salvaTokenAccesso(datiToken.accessToken)
                    datiToken.refreshToken?.let { sessionManager.salvaRefreshToken(it) }

                    // Decodifica il JWT a mano per estrarre le informazioni dell'utente
                    val payloadCodificato = datiToken.accessToken.split(".")[1]
                    val stringaDecodificata = String(Base64.decode(payloadCodificato, Base64.URL_SAFE))
                    val jsonPayload = JSONObject(stringaDecodificata)

                    val preferredUsername = jsonPayload.optString("preferred_username", usernameInput)

                    // Estrae l'array dei ruoli del realm realm_access.roles
                    val realmAccess = jsonPayload.optJSONObject("realm_access")
                    val ruoliArray = realmAccess?.optJSONArray("roles")

                    var ruoloPrincipale = "ROLE_VIAGGIATORE" // Default
                    if (ruoliArray != null) {
                        for (i in 0 until ruoliArray.length()) {
                            val r = ruoliArray.getString(i)
                            if (r == "ADMIN") {
                                ruoloPrincipale = "ROLE_ADMIN"
                                break
                            } else if (r == "ORGANIZZATORE") {
                                ruoloPrincipale = "ROLE_ORGANIZZATORE"
                                break
                            }
                        }
                    }

                    // Manca solo l'id utente del database, per ora si mette una stringa provvisoria,
                    // lo si sincronizzerà subito dopo chiamando il backend utenti/me
                    sessionManager.salvaDatiUtente(preferredUsername, ruoloPrincipale, "PROVVISORIO")

                    // Login completato con successo
                    _statoUi.value = StatoAuth.Successo(ruoloPrincipale)

                } else {
                    // Errore di credenziali (es. 401 o 400 da Keycloak)
                    _statoUi.value = StatoAuth.Errore("Credenziali non valide. Riprova!")
                }

            } catch (e: Exception) {
                // Errore di rete (es. Server Spring Boot o Keycloak spento)
                _statoUi.value = StatoAuth.Errore("Impossibile connettersi al server. Verifica la connessione.")
            }
        }
    }

    fun resetStato() {
        _statoUi.value = StatoAuth.Iniziale
    }
}