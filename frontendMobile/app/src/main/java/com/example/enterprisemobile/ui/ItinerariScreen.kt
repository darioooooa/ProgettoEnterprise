package com.example.enterprisemobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import com.example.enterprisemobile.viewmodels.ItinerarioViewModel

@Composable
fun ItinerariScreen(viewModel: ItinerarioViewModel) {
    val lista by viewModel.itinerari.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val errore by viewModel.errorMessage.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.caricaItinerari()
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> {
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
                    text = "Nessun itinerario trovato.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lista) { itinerario ->
                        ItinerarioItem(itinerario)
                    }
                }
            }
        }
    }
}

@Composable
fun ItinerarioItem(itinerario: ItinerarioPreferitoDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    }
}