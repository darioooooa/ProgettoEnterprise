package com.example.enterprisemobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import com.example.enterprisemobile.viewmodels.ItinerarioViewModel
import com.example.enterprisemobile.model.Visibilita

@Composable
fun ItinerariScreen(viewModel: ItinerarioViewModel) {
    val lista by viewModel.itinerari.collectAsState()
    val inCaricamento by viewModel.isLoading.collectAsState()
    val errore by viewModel.errorMessage.collectAsState()

    
    var mostraFinestraCreazione by remember { mutableStateOf(false) }
    var nomeDelNuovoItinerario by remember { mutableStateOf("") }
    var visibilitaScelta by remember { mutableStateOf(Visibilita.PRIVATA) }

    LaunchedEffect(Unit) {
        viewModel.caricaItinerari()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostraFinestraCreazione = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crea nuovo itinerario")
            }
        }
    ) { marginiInterni ->
        Box(modifier = Modifier.fillMaxSize().padding(marginiInterni).padding(16.dp)) {
            when {
                inCaricamento -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errore != null -> {
                    Text(
                        text = errore ?: "Errore sconosciuto",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                lista.isEmpty() -> {
                    Text(
                        text = "Nessun itinerario trovato. Creane uno!",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(lista) { singoloItinerario ->
                            singoloItinerario.idItinerario?.let { numeroIdentificativo ->
                                ItinerarioItem(
                                    itinerario = singoloItinerario,
                                    azioneDiCancellazione = {
                                        viewModel.avviaEliminazioneItinerario(numeroIdentificativo)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Finestra a comparsa per creare l'itinerario
    if (mostraFinestraCreazione) {
        AlertDialog(
            onDismissRequest = { mostraFinestraCreazione = false },
            title = { Text("Crea Nuovo Itinerario") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nomeDelNuovoItinerario,
                        onValueChange = { nomeDelNuovoItinerario = it },
                        label = { Text("Nome Itinerario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Visibilità:")

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = visibilitaScelta == Visibilita.PRIVATA,
                            onClick = { visibilitaScelta = Visibilita.PRIVATA }
                        )
                        Text("Privata")
                        Spacer(modifier = Modifier.width(8.dp))

                        RadioButton(
                            selected = visibilitaScelta == Visibilita.PUBBLICA,
                            onClick = { visibilitaScelta = Visibilita.PUBBLICA }
                        )
                        Text("Pubblica")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nomeDelNuovoItinerario.isNotBlank()) {
                            viewModel.creaItinerario(nomeDelNuovoItinerario, visibilitaScelta)
                            // Resetta i campi e chiude il dialog
                            nomeDelNuovoItinerario = ""
                            visibilitaScelta = Visibilita.PRIVATA
                            mostraFinestraCreazione = false
                        }
                    }
                ) {
                    Text("Crea")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraFinestraCreazione = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun ItinerarioItem(itinerario: ItinerarioPreferitoDTO, azioneDiCancellazione: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = itinerario.nome,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Visibilità: ${itinerario.visibilita}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = azioneDiCancellazione) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Cancella Itinerario",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}