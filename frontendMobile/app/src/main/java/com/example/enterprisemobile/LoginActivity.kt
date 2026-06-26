package com.example.enterprisemobile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.enterprisemobile.viewmodels.AuthViewModel
import com.example.enterprisemobile.viewmodels.StatoAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginActivity(viewModel: AuthViewModel) {
    val context = LocalContext.current

    // Osserva lo stato esposto dal ViewModel via StateFlow
    val statoUi by viewModel.statoUi.collectAsState()

    // Stati locali per i buffer di input dei campi di testo
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Gestione reattiva dell'effetto collaterale al cambio di stato su Successo
    LaunchedEffect(key1 = statoUi) {
        if (statoUi is StatoAuth.Successo) {
            val ruolo = (statoUi as StatoAuth.Successo).ruolo

            // CONTROLLO DEL RUOLO
            if (ruolo == "ROLE_VIAGGIATORE") {
                Toast.makeText(context, "Benvenuto Viaggiatore!", Toast.LENGTH_SHORT).show()

                // Navigazione verso la Home del Viaggiatore
                val intent = android.content.Intent(context, HomeViaggiatoreActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            } else {
                // Se il login va bene ma non è un viaggiatore, gestisci qui (es. mostra un messaggio)
                Toast.makeText(context, "Accesso riuscito come: $ruolo, ma questa sezione è riservata ai viaggiatori.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.titolo_login),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo input username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(text = stringResource(id = R.string.label_username)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = statoUi !is StatoAuth.Caricamento
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo input password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = stringResource(id = R.string.label_password)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = statoUi !is StatoAuth.Caricamento
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Controllo dello stato per decidere se mostrare la progress bar o il bottone
            if (statoUi is StatoAuth.Caricamento) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            } else {
                Button(
                    onClick = { viewModel.eseguiLogin(context, username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.bottone_accedi),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Renderizzazione dinamica dell'errore
            if (statoUi is StatoAuth.Errore) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (statoUi as StatoAuth.Errore).messaggio,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}