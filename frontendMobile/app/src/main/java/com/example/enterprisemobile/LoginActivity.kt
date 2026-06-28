package com.example.enterprisemobile

import com.example.enterprisemobile.ui.theme.DarkNavy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.enterprisemobile.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginActivity(viewModel: AuthViewModel, onTornaIndietro: () -> Unit, onNavigaARegistrazione: () -> Unit) {
    val context = LocalContext.current
    val statoUi by viewModel.statoUi.collectAsState()
    val statoRecupero by viewModel.statoRecupero.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibile by remember { mutableStateOf(false) }

    var mostraModaleRecupero by remember { mutableStateOf(false) }
    var emailRecupero by remember { mutableStateOf("") }

    LaunchedEffect(statoUi) {
        if (statoUi is StatoAuth.Successo) {
            val ruolo = (statoUi as StatoAuth.Successo).ruolo
            val intent = if (ruolo == "ROLE_ORGANIZZATORE" || ruolo == "ORGANIZZATORE") {
                android.content.Intent(context, HomeOrganizzatoreActivity::class.java).apply { putExtra("CHIAVE_USERNAME", username) }
            } else {
                android.content.Intent(context, HomeViaggiatoreActivity::class.java)
            }
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "← Torna alla pagina iniziale",
            color = Color.LightGray,
            modifier = Modifier.statusBarsPadding().clickable { if (statoUi !is StatoAuth.Caricamento) onTornaIndietro() }.padding(vertical = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Bentornato", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Accedi e prenota il tuo prossimo viaggio!", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Identificativo utente") }, modifier = Modifier.fillMaxWidth(),
                enabled = statoUi !is StatoAuth.Caricamento
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisibile) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(), enabled = statoUi !is StatoAuth.Caricamento,
                trailingIcon = {
                    val icona = if (passwordVisibile) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisibile = !passwordVisibile }) { Icon(icona, null) }
                }
            )

            // Link Recupero Password
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Hai dimenticato la password?", color = Color.LightGray, fontSize = 13.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        viewModel.resetStatoRecupero()
                        emailRecupero = ""
                        mostraModaleRecupero = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (statoUi is StatoAuth.Caricamento) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { viewModel.eseguiLogin(context, username, password) }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Inizia l'avventura", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (statoUi is StatoAuth.Errore) {
                Text(text = (statoUi as StatoAuth.Errore).messaggio, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }

            // Footer Cambio Schermata
            Row(modifier = Modifier.padding(top = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Non hai un account? ", color = Color.Gray)
                Text("Registrati", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigaARegistrazione() })
            }
        }
    }

    // 👤 MODALE RECUPERO PASSWORD (DIALOG)
    if (mostraModaleRecupero) {
        Dialog(onDismissRequest = { if (statoRecupero !is StatoRecupero.Caricamento) mostraModaleRecupero = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Recupera password", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Inserisci l'indirizzo email associato al tuo account.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedTextField(
                        value = emailRecupero, onValueChange = { emailRecupero = it },
                        label = { Text("La tua email...") }, modifier = Modifier.fillMaxWidth(),
                        enabled = statoRecupero !is StatoRecupero.Caricamento,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )

                    if (statoRecupero is StatoRecupero.Successo) {
                        Text("Link inviato! Controlla la posta.", color = Color(0xFF10b981), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 12.dp))
                    }
                    if (statoRecupero is StatoRecupero.Errore) {
                        Text((statoRecupero as StatoRecupero.Errore).messaggio, color = Color.Red, modifier = Modifier.padding(top = 12.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { mostraModaleRecupero = false }, enabled = statoRecupero !is StatoRecupero.Caricamento) {
                            Text("Chiudi", color = Color.Gray)
                        }
                        Button(
                            onClick = { viewModel.eseguiRecuperoPassword(context, emailRecupero) },
                            enabled = emailRecupero.isNotBlank() && statoRecupero !is StatoRecupero.Successo && statoRecupero !is StatoRecupero.Caricamento,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                        ) {
                            if (statoRecupero is StatoRecupero.Caricamento) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            else Text("Invia link")
                        }
                    }
                }
            }
        }
    }
}