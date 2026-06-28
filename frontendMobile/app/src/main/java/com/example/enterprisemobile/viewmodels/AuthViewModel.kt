package com.example.enterprisemobile.viewmodels

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.api.UtenteApiService
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

    private val _statoRegistrazione = MutableStateFlow<StatoRegistrazione>(StatoRegistrazione.Iniziale)
    val statoRegistrazione: StateFlow<StatoRegistrazione> = _statoRegistrazione.asStateFlow()

    private val _statoRecupero = MutableStateFlow<StatoRecupero>(StatoRecupero.Iniziale)
    val statoRecupero: StateFlow<StatoRecupero> = _statoRecupero.asStateFlow()

    // Esegue il tentativo di accesso parlando con Keycloak e salvando la sessione
    fun eseguiLogin(context: Context, usernameInput: String, passwordInput: String) {
        val usernamePulito = usernameInput.trim()
        val passwordPulita = passwordInput.trim()

        if (usernamePulito.isBlank() || passwordPulita.isBlank()) {
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
                    "username" to usernamePulito,
                    "password" to passwordPulita
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

                    val preferredUsername = jsonPayload.optString("preferred_username", usernamePulito)

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

                    try {
                        // Sincronizzazione con spring boot (Chiamata a utenti/me)
                        val utenteService = RetrofitClient.ottieniUtenteService(context)
                        val rispostaBackend = utenteService.ottieniMioProfilo()

                        if (rispostaBackend.isSuccessful && rispostaBackend.body() != null) {
                            val datiUtenteDatabase = rispostaBackend.body()!!

                            sessionManager.salvaDatiUtente(preferredUsername, ruoloPrincipale, datiUtenteDatabase.id.toString())

                            // Ora dichiara il successo completo del login
                            _statoUi.value = StatoAuth.Successo(ruoloPrincipale)
                        } else {
                            _statoUi.value = StatoAuth.Errore("Errore durante l'allineamento del profilo con il database.")
                        }
                    } catch (e: Exception) {
                        _statoUi.value = StatoAuth.Errore("Impossibile contattare il server dell'applicazione per la sincronizzazione.")
                    }

                } else {
                    _statoUi.value = StatoAuth.Errore("Credenziali non valide. Riprova!")
                }

            } catch (e: Exception) {
                // Errore di rete (es. Server Spring Boot o Keycloak spento)
                _statoUi.value = StatoAuth.Errore("Impossibile connettersi al server. Verifica la connessione.")
            }
        }
    }

    fun eseguiRegistrazione(context: Context, username: String, nome: String, cognome: String, email: String, password: String) {
        val usernamePulito = username.trim()
        val nomePulito = nome.trim()
        val cognomePulito = cognome.trim()
        val emailPulito = email.trim()
        val passwordPulita = password.trim()

        if (usernamePulito.isBlank() || nomePulito.isBlank() || cognomePulito.isBlank() || emailPulito.isBlank() || passwordPulita.isBlank()) {
            _statoRegistrazione.value = StatoRegistrazione.Errore("Tutti i campi sono obbligatori.")
            return
        }

        _statoRegistrazione.value = StatoRegistrazione.Caricamento

        viewModelScope.launch {
            try {
                val authService = RetrofitClient.ottieniAuthService(context)
                val dati = mapOf(
                    "username" to usernamePulito,
                    "nome" to nomePulito,
                    "cognome" to cognomePulito,
                    "email" to emailPulito,
                    "password" to passwordPulita,
                    "ruolo" to "VIAGGIATORE"
                )

                val risposta = authService.registraUtente(dati)
                if (risposta.isSuccessful) {
                    _statoRegistrazione.value = StatoRegistrazione.Successo
                } else {
                    _statoRegistrazione.value = StatoRegistrazione.Errore("Sembra che questa email o username siano già registrati.")
                }
            } catch (e: Exception) {
                _statoRegistrazione.value = StatoRegistrazione.Errore("Errore di rete. Impossibile contattare il server.")
            }
        }
    }

    fun eseguiRecuperoPassword(context: Context, email: String) {
        val emailPulita = email.trim()
        _statoRecupero.value = StatoRecupero.Caricamento

        viewModelScope.launch {
            try {
                val utenteService = RetrofitClient.ottieniUtenteService(context)
                val risposta = utenteService.recuperaPassword(emailPulita)
                if (risposta.isSuccessful) {
                    _statoRecupero.value = StatoRecupero.Successo
                } else {
                    _statoRecupero.value = StatoRecupero.Errore("Si è verificato un errore. Verifica che l'email sia registrata.")
                }
            } catch (e: Exception) {
                _statoRecupero.value = StatoRecupero.Errore("Impossibile connettersi al server.")
            }
        }
    }

    fun resetStato() {
        _statoUi.value = StatoAuth.Iniziale
    }

    fun resetStatoRegistrazione() {
        _statoRegistrazione.value = StatoRegistrazione.Iniziale
    }

    fun resetStatoRecupero() {
        _statoRecupero.value = StatoRecupero.Iniziale
    }
}

sealed class StatoRegistrazione {
    object Iniziale : StatoRegistrazione()
    object Caricamento : StatoRegistrazione()
    object Successo : StatoRegistrazione()
    data class Errore(val messaggio: String) : StatoRegistrazione()
}

sealed class StatoRecupero {
    object Iniziale : StatoRecupero()
    object Caricamento : StatoRecupero()
    object Successo : StatoRecupero()
    data class Errore(val messaggio: String) : StatoRecupero()
}