package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggato()) {
            reindirizzaAHome(sessionManager.ottieniRuolo(), sessionManager.ottieniUsername())
            return
        }

        setContent {
            EnterpriseMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var schermataAttuale by remember { mutableStateOf(SchermataIniziale.LANDING) }

                    when (schermataAttuale) {
                        SchermataIniziale.LANDING -> SchermataLanding(
                            onNavigaALogin = { schermataAttuale = SchermataIniziale.LOGIN },
                            onNavigaARegistrazione = { schermataAttuale = SchermataIniziale.REGISTRAZIONE }
                        )
                        SchermataIniziale.LOGIN -> LoginActivity(
                            viewModel = authViewModel,
                            onTornaIndietro = { schermataAttuale = SchermataIniziale.LANDING },
                            onNavigaARegistrazione = { schermataAttuale = SchermataIniziale.REGISTRAZIONE }
                        )
                        SchermataIniziale.REGISTRAZIONE -> RegistrazioneActivity(
                            viewModel = authViewModel,
                            onTornaIndietro = { schermataAttuale = SchermataIniziale.LANDING },
                            onTornaAlLogin = { schermataAttuale = SchermataIniziale.LOGIN }
                        )
                    }
                }
            }
        }
    }

    private fun reindirizzaAHome(ruolo: String?, username: String?) {
        val intentHome = when (ruolo) {
            "ROLE_ADMIN" -> Intent(this, AdminActivity::class.java)  // ✅ AGGIUNGI QUESTO
            "ROLE_ORGANIZZATORE" -> Intent(this, HomeOrganizzatoreActivity::class.java).apply {
                putExtra("CHIAVE_USERNAME", username)
            }
            "ROLE_VIAGGIATORE" -> Intent(this, HomeViaggiatoreActivity::class.java)
            else -> {
                // Ruolo non valido, fai logout
                SessionManager(this).cancellaSessione()
                return
            }
        }

        intentHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentHome)
        finish()
    }
}