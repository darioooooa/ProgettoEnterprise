package com.example.enterprisemobile

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.ProfiloViewModel

class ProfiloActivity : ComponentActivity() {
    private val viewModel: ProfiloViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                ProfiloContent(viewModel)
            }
        }
    }
}

@Composable
fun ProfiloContent(viewModel: ProfiloViewModel) {
    val context = LocalContext.current
    val profilo = viewModel.datiProfilo

    EnterpriseScaffold(
        titolo = "IL MIO PROFILO",
        nomeUtente = viewModel.sessionManager.ottieniUsername() ?: "Utente",
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(DarkNavy).padding(innerPadding)) {

            if (viewModel.isCaricamento) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentBlue)
            } else if (profilo != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Surface(
                        shape = CircleShape,
                        color = AccentBlue.copy(alpha = 0.2f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.padding(24.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(profilo.nomeCompleto ?: "${profilo.nome} ${profilo.cognome}", color = WhiteText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("@${profilo.username}", color = AccentBlue, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(32.dp))

                                       SchedaDettaglio(icona = Icons.Filled.Email, titolo = "Email", valore = profilo.email)
                    Spacer(modifier = Modifier.height(16.dp))
                    SchedaDettaglio(icona = Icons.Filled.Badge, titolo = "Ruolo", valore = profilo.ruolo?.replace("ROLE_", "") ?: "Non definito")

                    Spacer(modifier = Modifier.weight(1f))

                }
            } else {
                Text(viewModel.messaggioErrore ?: "Errore sconosciuto", color = DangerRed, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SchedaDettaglio(icona: ImageVector, titolo: String, valore: String) {
    Surface(color = CardOverlay, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icona, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titolo, color = Color.Gray, fontSize = 14.sp)
                Text(valore, color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}