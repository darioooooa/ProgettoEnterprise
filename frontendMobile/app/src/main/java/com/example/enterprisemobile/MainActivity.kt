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
                    // Stato che controlla quale vista mostrare (Landing, Login o Registrazione)
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
        val intentHome = if (ruolo == "ROLE_ORGANIZZATORE") {
            Intent(this, HomeOrganizzatoreActivity::class.java).apply {
                putExtra("CHIAVE_USERNAME", username)
            }
        } else {
            Intent(this, HomeViaggiatoreActivity::class.java)
        }
        startActivity(intentHome)
        finish()
    }
}