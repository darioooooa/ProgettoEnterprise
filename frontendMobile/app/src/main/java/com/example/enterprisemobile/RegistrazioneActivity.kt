package com.example.enterprisemobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.viewmodels.AuthViewModel
import com.example.enterprisemobile.viewmodels.StatoRegistrazione

@Composable
fun RegistrazioneActivity(viewModel: AuthViewModel, onTornaIndietro: () -> Unit, onTornaAlLogin: () -> Unit) {
    val context = LocalContext.current
    val statoReg by viewModel.statoRegistrazione.collectAsState()

    var username by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        if (statoReg !is StatoRegistrazione.Successo) {
            Text(
                text = "← Torna alla pagina iniziale", color = Color.LightGray,
                modifier = Modifier.statusBarsPadding().clickable { if (statoReg !is StatoRegistrazione.Caricamento) onTornaIndietro() }.padding(vertical = 8.dp)
            )
        }

        if (statoReg is StatoRegistrazione.Successo) {
            // Vista di successo
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("✉️", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Controlla la tua casella di posta!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Abbiamo inviato un link di verifica all'indirizzo:", color = Color.Gray, textAlign = TextAlign.Center)
                Text(email, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Per attivare il tuo account e poter accedere, clicca sul link che troverai all'interno del messaggio inviato.\n\nSe non vedi il messaggio, controlla nella cartella Spam.",
                    color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.resetStatoRegistrazione(); onTornaAlLogin() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Torna alla pagina di accesso")
                }
            }
        } else {
            // Modulo di registrazione
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center).verticalScroll(rememberScrollState()).padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Crea il tuo profilo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "Entra a far parte della community dei viaggiatori", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), enabled = statoReg !is StatoRegistrazione.Caricamento)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth(), enabled = statoReg !is StatoRegistrazione.Caricamento)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = cognome, onValueChange = { cognome = it }, label = { Text("Cognome") }, modifier = Modifier.fillMaxWidth(), enabled = statoReg !is StatoRegistrazione.Caricamento)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Indirizzo email") }, modifier = Modifier.fillMaxWidth(), enabled = statoReg !is StatoRegistrazione.Caricamento)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Scegli una password") },
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(), enabled = statoReg !is StatoRegistrazione.Caricamento
                )

                if (statoReg is StatoRegistrazione.Errore) {
                    Text(text = (statoReg as StatoRegistrazione.Errore).messaggio, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (statoReg is StatoRegistrazione.Caricamento) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = { viewModel.eseguiRegistrazione(context, username, nome, cognome, email, password) }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("Registrati e parti", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Row(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Hai già un account? ", color = Color.Gray)
                    Text("Torna al login", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onTornaAlLogin() })
                }
            }
        }
    }
}