package com.example.enterprisemobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        if (statoReg !is StatoRegistrazione.Successo) {
            Text(
                text = "← Torna alla pagina iniziale",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .statusBarsPadding()
                    .clickable { if (statoReg !is StatoRegistrazione.Caricamento) onTornaIndietro() }
                    .padding(vertical = 8.dp)
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

                Text(
                    text = "Controlla la tua casella di posta!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Abbiamo inviato un link di verifica all'indirizzo:",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = email,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Per attivare il tuo account e poter accedere, clicca sul link che troverai all'interno del messaggio inviato.\n\nSe non vedi il messaggio, controlla nella cartella Spam.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.resetStatoRegistrazione(); onTornaAlLogin() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Torna alla pagina di accesso", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Modulo di registrazione
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crea il tuo profilo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Entra a far parte della community dei viaggiatori",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = { viewModel.eseguiRegistrazione(context, username, nome, cognome, email, password) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Registrati e parti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Hai già un account? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    Text(
                        text = "Torna al login",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onTornaAlLogin() }
                    )
                }
            }
        }
    }
}