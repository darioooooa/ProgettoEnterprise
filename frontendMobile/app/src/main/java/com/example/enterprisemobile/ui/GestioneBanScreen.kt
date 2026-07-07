package com.example.enterprisemobile.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.model.UtenteBannatoDTO
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.AdminViewModel

@Composable
fun GestioneBanScreen(viewModel: AdminViewModel) {
    val context = LocalContext.current

    val utentiBannati by viewModel.utentiBannati.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val paginaCorrente by viewModel.paginaCorrenteBan.observeAsState(0)
    val totalePagine by viewModel.totalePagineBan.observeAsState(0)
    val totaleElementi by viewModel.totaleElementiBan.observeAsState(0)

    var mostraConfermaRiattiva by remember { mutableStateOf(false) }
    var utenteDaRiattivare by remember { mutableStateOf<UtenteBannatoDTO?>(null) }
    var queryRicerca by remember { mutableStateOf("") }

    // Carica utenti bannati all'avvio
    LaunchedEffect(Unit) {
        viewModel.caricaUtentiBannati(page = 0)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Dialog conferma riattivazione
    if (mostraConfermaRiattiva && utenteDaRiattivare != null) {
        AlertDialog(
            onDismissRequest = {
                mostraConfermaRiattiva = false
                utenteDaRiattivare = null
            },
            title = { Text("Conferma Riattivazione", fontWeight = FontWeight.Bold, color = WhiteText) },
            text = {
                Text(
                    "Sei sicuro di voler riattivare l'utente ${utenteDaRiattivare?.username}?\n\n" +
                            "L'utente potrà nuovamente accedere alla piattaforma.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        utenteDaRiattivare?.let { utente ->
                            viewModel.riattivaUtente(
                                id = utente.id,
                                onSuccess = {
                                    Toast.makeText(context, "✅ Utente riattivato!", Toast.LENGTH_SHORT).show()
                                    mostraConfermaRiattiva = false
                                    utenteDaRiattivare = null
                                },
                                onError = { err ->
                                    Toast.makeText(context, "❌ Errore: $err", Toast.LENGTH_LONG).show()
                                    mostraConfermaRiattiva = false
                                    utenteDaRiattivare = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("✅ Riattiva", color = WhiteText) }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostraConfermaRiattiva = false
                    utenteDaRiattivare = null
                }) {
                    Text("Annulla", color = Color.Gray)
                }
            },
            containerColor = DarkNavy
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Gestione Sospensioni 🚫",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Utenti bannati: $totaleElementi",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Barra di ricerca
        OutlinedTextField(
            value = queryRicerca,
            onValueChange = { queryRicerca = it },
            label = { Text("Cerca per username o email...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WhiteText,
                unfocusedTextColor = WhiteText,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Color.Gray
            ),
            trailingIcon = {
                if (queryRicerca.isNotEmpty()) {
                    IconButton(onClick = {
                        queryRicerca = ""
                        viewModel.cercaUtentiBannati("")
                    }) {
                        Icon(Icons.Filled.Clear, "Cancella", tint = Color.Gray)
                    }
                }
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, "Cerca", tint = AccentBlue)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.cercaUtentiBannati(queryRicerca) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && utentiBannati.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentBlue
                )
            } else if (utentiBannati.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (queryRicerca.isNotEmpty()) Color.Gray else SuccessGreen,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (queryRicerca.isNotEmpty())
                            "Nessun risultato per \"$queryRicerca\""
                        else
                            "Nessun utente bannato",
                        color = WhiteText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (queryRicerca.isNotEmpty())
                            "Prova con altri termini di ricerca"
                        else
                            "Tutti gli utenti sono attivi",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(utentiBannati, key = { it.id }) { utente ->
                        CardBanUtente(
                            utente = utente,
                            onRiattiva = {
                                utenteDaRiattivare = utente
                                mostraConfermaRiattiva = true
                            }
                        )
                    }


                    if (utentiBannati.isNotEmpty()){
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.paginaPrecedenteBan() },
                                    enabled = paginaCorrente > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (paginaCorrente > 0) AccentBlue else Color.Gray
                                    )
                                ) {
                                    Text("Prec", fontSize = 14.sp)
                                }

                                Text(
                                    text = "Pagina ${paginaCorrente + 1} di $totalePagine",
                                    color = WhiteText,
                                    fontWeight = FontWeight.Medium
                                )

                                Button(
                                    onClick = { viewModel.paginaSuccessivaBan() },
                                    enabled = paginaCorrente < totalePagine - 1,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (paginaCorrente < totalePagine - 1) AccentBlue else Color.Gray
                                    )
                                ) {
                                    Text("Succ", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardBanUtente(utente: UtenteBannatoDTO, onRiattiva: () -> Unit) {
    Surface(
        color = CardOverlay,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "👤 ${utente.username}",
                        color = WhiteText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📧 ${utente.email}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    if (!utente.motivoSospensione.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ ${utente.motivoSospensione}",
                            color = DangerRed,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onRiattiva,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("✅ Riattiva", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}