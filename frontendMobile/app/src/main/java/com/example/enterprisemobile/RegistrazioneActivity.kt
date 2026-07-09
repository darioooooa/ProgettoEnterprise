package com.example.enterprisemobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.viewmodels.AuthViewModel
import com.example.enterprisemobile.viewmodels.StatoRegistrazione

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrazioneActivity(viewModel: AuthViewModel, onTornaIndietro: () -> Unit, onTornaAlLogin: () -> Unit) {
    val context = LocalContext.current
    val statoReg by viewModel.statoRegistrazione.collectAsState()
    val focusManager = LocalFocusManager.current
    val isCaricamento = statoReg is StatoRegistrazione.Caricamento

    var username by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confermaPassword by remember { mutableStateOf("") }

    var passwordVisibile by remember { mutableStateOf(false) }
    var erroreLocale by remember { mutableStateOf<String?>(null) }

    val outlinedTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 1f))
            .padding(24.dp)
    ) {
        if (statoReg !is StatoRegistrazione.Successo) {
            Text(
                text = "← Torna alla pagina iniziale",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .statusBarsPadding()
                    .clickable {
                        if (!isCaricamento) onTornaIndietro()
                    }
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

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCaricamento,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = outlinedTextFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Nome
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCaricamento,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = outlinedTextFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Cognome
                OutlinedTextField(
                    value = cognome,
                    onValueChange = { cognome = it },
                    label = { Text("Cognome") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCaricamento,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = outlinedTextFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Indirizzo email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCaricamento,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = outlinedTextFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        erroreLocale = null
                    },
                    label = { Text("Scegli una password") },
                    visualTransformation = if (passwordVisibile) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCaricamento,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = outlinedTextFieldColors,
                    trailingIcon = {
                        val icona = if (passwordVisibile) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisibile = !passwordVisibile }) {
                            Icon(icona, contentDescription = "Mostra/Nascondi password", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Conferma password
                OutlinedTextField(
                    value = confermaPassword,
                    onValueChange = {
                        confermaPassword = it
                        erroreLocale = null
                    },
                    label = { Text("Conferma password") },
                    visualTransformation = if (passwordVisibile) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCaricamento,
                    singleLine = true,
                    // Ultimo campo della tastiera (ImeAction.Done)
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (statoReg !is StatoRegistrazione.Caricamento) {
                            // Validazione prima dell'invio
                            if (password != confermaPassword) {
                                erroreLocale = "Le password inserite non coincidono."
                            } else {
                                viewModel.eseguiRegistrazione(context, username, nome, cognome, email, password)
                            }
                        }
                    }),
                    colors = outlinedTextFieldColors,
                    trailingIcon = {
                        val icona = if (passwordVisibile) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisibile = !passwordVisibile }, enabled = !isCaricamento) {
                            Icon(icona, contentDescription = "Mostra/Nascondi password", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )

                // Mostra prima gli errori locali (password non coincidenti) e poi quelli del server
                if (erroreLocale != null) {
                    Text(text = erroreLocale!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
                } else if (statoReg is StatoRegistrazione.Errore) {
                    Text(text = (statoReg as StatoRegistrazione.Errore).messaggio, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (statoReg is StatoRegistrazione.Caricamento) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = {
                            if (password != confermaPassword) {
                                erroreLocale = "Le password inserite non coincidono."
                            } else {
                                viewModel.eseguiRegistrazione(context, username, nome, cognome, email, password)
                            }
                        },
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
                        modifier = Modifier.clickable { if (!isCaricamento) onTornaAlLogin() }
                    )
                }
            }
        }
    }
}