package com.example.enterprisemobile.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.SuccessGreen
import com.example.enterprisemobile.viewmodels.ProgrammaViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SezioneAttivitaViaggioInPage(
    viewModel: ProgrammaViewModel,
    viaggioId: Long,
    isMioViaggio: Boolean,
    context: Context
) {
    val tappe by viewModel.attivita.collectAsState()

    // Sincronizzazione iniziale e caricamento delle attività native
    LaunchedEffect(viaggioId) {
        if (viaggioId != -1L) {
            viewModel.caricaAttivita(viaggioId)
        }
    }

    // Stati per controllare la visibilità dei vari DatePicker
    var dGiornoMinAperto by remember { mutableStateOf(false) }
    var dGiornoMaxAperto by remember { mutableStateOf(false) }
    var dInizioFormAperto by remember { mutableStateOf(false) }
    var dFineFormAperto by remember { mutableStateOf(false) }

    var filtriEspansi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titolo della sezione integrata
        Column {
            Text(
                text = "Itinerario e attività",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Consulta gli impegni e le tappe giornaliere",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        // Banner messaggi d'errore o successo interni
        viewModel.messaggioAvviso?.let { avviso ->
            val colore = if (viewModel.tipoAvviso == "successo") SuccessGreen else MaterialTheme.colorScheme.error
            Surface(
                color = colore.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(avviso, color = colore, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(
                        text = "×",
                        color = colore,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.messaggioAvviso = null }
                    )
                }
            }
        }

        // Sezione filtri di ricerca
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filtriEspansi = !filtriEspansi },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtra tappe del programma",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (filtriEspansi) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (filtriEspansi) "Riduci filtri" else "Espandi filtri",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = filtriEspansi) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = viewModel.filtroTitolo, onValueChange = { viewModel.filtroTitolo = it }, label = { Text("Titolo") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = viewModel.filtroPosizione, onValueChange = { viewModel.filtroPosizione = it }, label = { Text("Luogo") }, modifier = Modifier.weight(1f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = viewModel.filtroCostoMin, onValueChange = { viewModel.filtroCostoMin = it }, label = { Text("€ Min") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = viewModel.filtroCostoMax, onValueChange = { viewModel.filtroCostoMax = it }, label = { Text("€ Max") }, modifier = Modifier.weight(1f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Filtro dal giorno
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = viewModel.filtroOrarioInizioMin,
                                    onValueChange = {},
                                    label = { Text("Dal giorno") },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { dGiornoMinAperto = true }
                                )
                            }
                            // Filtro al giorno
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = viewModel.filtroOrarioInizioMax,
                                    onValueChange = {},
                                    label = { Text("Al giorno") },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { dGiornoMaxAperto = true }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { viewModel.pulisciFiltriAttivita(viaggioId) }) {
                                Text("Resetta", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.filtraAttivita(viaggioId) }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                                Text("Cerca")
                            }
                        }
                    }
                }
            }
        }

        // Form organizzatore: aggiunta / modifica tappa
        if (isMioViaggio) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (viewModel.attivitaInModifica) "✏️ Modifica attività" else "➕ Nuova attività nel programma",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(value = viewModel.titoloInput, onValueChange = { viewModel.titoloInput = it }, label = { Text("Titolo attività *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = viewModel.posizioneInput, onValueChange = { viewModel.posizioneInput = it }, label = { Text("Posizione / Luogo *") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Form data inizio tappa
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = viewModel.orarioInizioInput,
                                onValueChange = {},
                                label = { Text("Inizio *") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dInizioFormAperto = true }
                            )
                        }
                        // Form data fine tappa
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = viewModel.orarioFineInput,
                                onValueChange = {},
                                label = { Text("Fine *") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dFineFormAperto = true }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.costoInput,
                        onValueChange = { viewModel.costoInput = it },
                        label = { Text("Costo (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = viewModel.descrizioneInput, onValueChange = { viewModel.descrizioneInput = it }, label = { Text("Descrizione") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.attivitaInModifica) {
                            TextButton(onClick = { viewModel.pulisciStatoForm() }) {
                                Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(onClick = { viewModel.aggiungiTappaProgramma(context, viaggioId) }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                            Text(if (viewModel.attivitaInModifica) "Aggiorna" else "Salva tappa")
                        }
                    }
                }
            }
        }

        // Render e ciclo iterativo della lista delle tappe del programma
        if (tappe.isEmpty()) {
            Text(
                text = "Nessuna attività corrispondente ai criteri impostati.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            tappe.forEach { att ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(att.titolo, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (!att.descrizione.isNullOrBlank()) {
                            Text(att.descrizione, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🕒 ${att.orarioInizio.replace("T", " ")}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("📍 ${att.posizione}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text("💰 ${if (att.costo > 0) "€ ${att.costo}" else "Gratuito"}", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (isMioViaggio) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = "Modifica",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { viewModel.avviaModifica(tappa = att) }
                                        .padding(end = 16.dp)
                                )
                                Text(
                                    text = if (viewModel.idAttivitaDaEliminare == att.id) "⚠️ Confermi?" else "Elimina",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { viewModel.cancellaTappa(context, viaggioId, att.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Logica di paginazione
        if (viewModel.totalePagineAttivita > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.cambiaPagina(viaggioId, -1) },
                    enabled = viewModel.paginaAttivita > 0
                ) {
                    Text("←", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "${viewModel.paginaAttivita + 1} di ${viewModel.totalePagineAttivita}",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = { viewModel.cambiaPagina(viaggioId, 1) },
                    enabled = viewModel.paginaAttivita < viewModel.totalePagineAttivita - 1
                ) {
                    Text("→", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
    // Modali dei date picker

    // Selettore "dal giorno" (filtri) -> Formato YYYY-MM-DD
    if (dGiornoMinAperto) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dGiornoMinAperto = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val data = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.filtroOrarioInizioMin = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    dGiornoMinAperto = false
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dGiornoMinAperto = false }) { Text("Annulla") } }
        ) { DatePicker(state = state) }
    }

    // Selettore "al giorno" (filtri) -> Formato YYYY-MM-DD
    if (dGiornoMaxAperto) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dGiornoMaxAperto = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val data = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.filtroOrarioInizioMax = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    dGiornoMaxAperto = false
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dGiornoMaxAperto = false }) { Text("Annulla") } }
        ) { DatePicker(state = state) }
    }

    // Selettore "inizio tappa" (form) -> Formato YYYY-MM-DDTHH:MM (inizializzato a T08:00 di default)
    if (dInizioFormAperto) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dInizioFormAperto = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val data = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.orarioInizioInput = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T08:00"
                    }
                    dInizioFormAperto = false
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dInizioFormAperto = false }) { Text("Annulla") } }
        ) { DatePicker(state = state) }
    }

    // Selettore "fine tappa" (form) -> Formato YYYY-MM-DDTHH:MM (inizializzato a T20:00 di default)
    if (dFineFormAperto) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dFineFormAperto = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val data = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.orarioFineInput = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T20:00"
                    }
                    dFineFormAperto = false
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dFineFormAperto = false }) { Text("Annulla") } }
        ) { DatePicker(state = state) }
    }
}