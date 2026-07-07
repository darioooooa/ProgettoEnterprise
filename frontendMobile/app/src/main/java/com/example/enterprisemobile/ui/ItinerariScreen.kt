package com.example.enterprisemobile.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.DettaglioViaggioActivity
import com.example.enterprisemobile.model.InvitoSospesoDTO
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.model.Visibilita
import com.example.enterprisemobile.ui.theme.SuccessGreen
import com.example.enterprisemobile.viewmodels.ItinerarioViewModel

@Composable
fun ItinerariScreen(viewModel: ItinerarioViewModel) {
    val mieiItinerari by viewModel.itinerari.collectAsState()
    val itinerariCondivisi by viewModel.itinerariCondivisi.collectAsState()
    val invitiInSospeso by viewModel.invitiInSospeso.collectAsState()
    val inCaricamento by viewModel.isLoading.collectAsState()
    val errore by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // Stati per i dialog a comparsa
    var mostraModaleCreazione by remember { mutableStateOf(false) }
    var mostraModaleCondivisione by remember { mutableStateOf(false) }
    var idItinerarioDaCondividere by remember { mutableStateOf<Long?>(null) }

    var nuovoNome by remember { mutableStateOf("") }
    var nuovaVisibilita by remember { mutableStateOf(Visibilita.PRIVATA) }
    var emailDaInvitare by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.caricaItinerari()
    }

    LaunchedEffect(errore) {
        errore?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .pointerInput(inCaricamento) {
                    if (inCaricamento) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        nuovoNome = ""
                        nuovaVisibilita = Visibilita.PRIVATA
                        mostraModaleCreazione = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !inCaricamento,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )                ) {
                    Text("+ Crea nuovo itinerario", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Sezione 1: inviti pendenti
            if (invitiInSospeso.isNotEmpty()) {
                item {
                    Text("📩 Richieste in sospeso", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                itemsIndexed(invitiInSospeso, key = { index, invito ->
                    "invito-${invito.idItinerario}-${invito.proprietario}-${index}"
                }) { _, invito ->
                    CardInvitoSospeso(invito = invito, viewModel = viewModel, disabilitato = inCaricamento)
                }
            }

            // Sezione 2: miei itinerari
            item {
                Text("I miei itinerari", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (mieiItinerari.isEmpty() && !inCaricamento) {
                item {
                    Text("Nessun itinerario personale presente.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                }
            } else {
                items(mieiItinerari, key = { "mio-${it.idItinerario ?: it.nome.hashCode()}-${it.viaggiContenuti?.size ?: 0}" }) { itinerario ->
                    CardItinerarioCompleta(
                        itinerario = itinerario,
                        tuttiGliItinerari = mieiItinerari,
                        viewModel = viewModel,
                        disabilitato = inCaricamento,
                        onCondividiClick = {
                            idItinerarioDaCondividere = itinerario.idItinerario
                            emailDaInvitare = ""
                            mostraModaleCondivisione = true
                        }
                    )
                }
            }

            // Sezione 3: itinerari condivisi dagli utenti
            item {
                Text("🤝 Itinerari condivisi con me", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (itinerariCondivisi.isEmpty() && !inCaricamento) {
                item {
                    Text("Nessun itinerario condiviso con te.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                }
            } else {
                itemsIndexed(itinerariCondivisi, key = { index, condiviso ->
                    val idSicuro = condiviso.idItinerario ?: 0L
                    "condiviso-${idSicuro}-${condiviso.viaggiContenuti?.size ?: 0}-${index}"
                }) { _, condiviso ->
                    CardItinerarioCondivisoCompleta(itinerario = condiviso, disabilitato = inCaricamento)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        if (inCaricamento) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
        }
    }

    // Modale creazione
    if (mostraModaleCreazione) {
        AlertDialog(
            onDismissRequest = { if (!inCaricamento) mostraModaleCreazione = false },
            title = { Text("Nuovo itinerario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nuovoNome, onValueChange = { nuovoNome = it },
                        label = { Text("Nome dell'itinerario") }, modifier = Modifier.fillMaxWidth(), enabled = !inCaricamento
                    )
                    Column {
                        Text("Visibilità:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = nuovaVisibilita == Visibilita.PRIVATA, onClick = { nuovaVisibilita = Visibilita.PRIVATA }, enabled = !inCaricamento)
                            Text("Privata (solo per me)", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = nuovaVisibilita == Visibilita.PUBBLICA, onClick = { nuovaVisibilita = Visibilita.PUBBLICA }, enabled = !inCaricamento)
                            Text("Pubblica (visibile a tutti)", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nuovoNome.trim().isBlank()) {
                        Toast.makeText(context, "Inserisci un nome per l'itinerario!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.creaItinerario(nuovoNome.trim(), nuovaVisibilita)
                        mostraModaleCreazione = false
                    }
                }, enabled = !inCaricamento) { Text("Crea") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleCreazione = false }, enabled = !inCaricamento) { Text("Annulla") } }
        )
    }

    // Modale condivisione
    if (mostraModaleCondivisione) {
        AlertDialog(
            onDismissRequest = { if (!inCaricamento) mostraModaleCondivisione = false },
            title = { Text("Invita Collaboratore", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inserisci l'email dell'utente con cui collaborare.", fontSize = 13.sp)
                    OutlinedTextField(value = emailDaInvitare, onValueChange = { emailDaInvitare = it }, label = { Text("Email dell'amico") }, modifier = Modifier.fillMaxWidth(), enabled = !inCaricamento)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val idLista = idItinerarioDaCondividere
                    if (emailDaInvitare.trim().isBlank()) {
                        Toast.makeText(context, "Inserisci un email!", Toast.LENGTH_SHORT).show()
                    } else if (idLista != null) {
                        viewModel.invitaCollaboratoreInItinerario(idLista, emailDaInvitare.trim()) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        mostraModaleCondivisione = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen), enabled = !inCaricamento) { Text("Invia invito", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { mostraModaleCondivisione = false }, enabled = !inCaricamento) { Text("Annulla") } }
        )
    }
}

@Composable
fun CardItinerarioCompleta(
    itinerario: ItinerarioPreferitoDTO,
    tuttiGliItinerari: List<ItinerarioPreferitoDTO>,
    viewModel: ItinerarioViewModel,
    disabilitato: Boolean,
    onCondividiClick: () -> Unit
) {
    var mostraConfermaElimina by remember { mutableStateOf(false) }
    var espansa by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !disabilitato) { espansa = !espansa },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("#ITN-${itinerario.idItinerario ?: 0L}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    SuggestionChip(
                        onClick = { itinerario.idItinerario?.let { viewModel.cambiaVisibilitaItinerario(it, itinerario.visibilita) } },
                        label = { Text("${itinerario.visibilita.name} ✏️", fontSize = 10.sp) }, enabled = !disabilitato
                    )
                    if (itinerario.inCondivisione == true) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                            Text("👥 In condivisione", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onCondividiClick, enabled = !disabilitato) { Icon(Icons.Filled.Share, "Condividi",tint = MaterialTheme.colorScheme.secondary) }
                    IconButton(onClick = { mostraConfermaElimina = true }, enabled = !disabilitato) { Icon(Icons.Filled.Delete, "Elimina", tint = MaterialTheme.colorScheme.error) }
                }
            }

            // Titolo e freccia di espansione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = itinerario.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                Icon(
                    imageVector = if (espansa) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (espansa) "Riduci" else "Espandi",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = espansa) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    val viajes = itinerario.viaggiContenuti ?: emptyList()
                    if (viajes.isEmpty()) {
                        Text("Nessun viaggio aggiunto.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic, fontSize = 13.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            viajes.forEach { viaggio ->
                                RowViaggioContenuto(
                                    viaggio = viaggio,
                                    idItinerarioSorgente = itinerario.idItinerario ?: 0L,
                                    tuttiGliItinerari = tuttiGliItinerari,
                                    viewModel = viewModel,
                                    disabilitato = disabilitato
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostraConfermaElimina) {
        AlertDialog(
            onDismissRequest = { mostraConfermaElimina = false },
            title = { Text("Elimina itinerario") },
            text = { Text("Sei sicuro di voler cancellare interamente l'itinerario \"${itinerario.nome}\"?") },
            confirmButton = {
                Button(onClick = {
                    itinerario.idItinerario?.let { viewModel.avviaEliminazioneItinerario(it) }
                    mostraConfermaElimina = false
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Elimina") }
            },
            dismissButton = { TextButton(onClick = { mostraConfermaElimina = false }) { Text("Annulla") } }
        )
    }
}

@Composable
fun CardItinerarioCondivisoCompleta(itinerario: ItinerarioPreferitoDTO, disabilitato: Boolean) {
    val context = LocalContext.current

    var espansa by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !disabilitato) { espansa = !espansa }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                    Text("🤝 Condiviso", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
                Text("Proprietario:", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(itinerario.proprietarioUsername ?: "Anonimo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Titolo e freccia di espansione per condivisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = itinerario.nome, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                Icon(
                    imageVector = if (espansa) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (espansa) "Riduci" else "Espandi",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            AnimatedVisibility(visible = espansa) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    val viaggi = itinerario.viaggiContenuti ?: emptyList()
                    if (viaggi.isEmpty()) {
                        Text("Nessun viaggio aggiunto in questa lista.", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f), fontStyle = FontStyle.Italic, fontSize = 13.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            viaggi.forEach { viaggio ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !disabilitato) {
                                            val idPassato = viaggio.id ?: 0L
                                            if (idPassato > 0) {
                                                val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                                                    putExtra("VIAGGIO_ID", idPassato)
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                Toast.makeText(context, "Errore: id viaggio non valido", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column {
                                        Text(viaggio.titolo, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("📍 ${viaggio.destinazione}", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), fontSize = 12.sp)
                                    }
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
fun CardInvitoSospeso(invito: InvitoSospesoDTO, viewModel: ItinerarioViewModel, disabilitato: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(invito.nomeItinerario, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Proprietario: ${invito.proprietario}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.accettaInvitoCollaborazione(invito.idItinerario) }, enabled = !disabilitato) {
                    Icon(Icons.Filled.Check, "Accetta", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { viewModel.rifiutaInvitoCollaborazione(invito.idItinerario) }, enabled = !disabilitato) {
                    Icon(Icons.Filled.Close, "Rifiuta", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun RowViaggioContenuto(
    viaggio: ViaggioDTO,
    idItinerarioSorgente: Long,
    tuttiGliItinerari: List<ItinerarioPreferitoDTO>,
    viewModel: ItinerarioViewModel,
    disabilitato: Boolean
) {
    val context = LocalContext.current
    val chiaveMenu = "$idItinerarioSorgente-${viaggio.id}"
    val menuSpostaAperto = viewModel.menuSpostaAperto[chiaveMenu] ?: false
    var mostraConfermaRimuovi by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !disabilitato) {
                    val idPassato = viaggio.id ?: 0L
                    if (idPassato > 0) {
                        val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                            putExtra("VIAGGIO_ID", idPassato)
                        }
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Errore: id viaggio non valido", Toast.LENGTH_SHORT).show()
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(viaggio.titolo, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("📍 ${viaggio.destinazione}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = { if (!disabilitato) viewModel.menuSpostaAperto[chiaveMenu] = !menuSpostaAperto }, enabled = !disabilitato) {
                    Icon(Icons.Filled.Refresh, "Sposta", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { mostraConfermaRimuovi = true }, enabled = !disabilitato) {
                    Icon(Icons.Filled.Close, "Rimuovi", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        AnimatedVisibility(visible = menuSpostaAperto && !disabilitato) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Sposta in:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    tuttiGliItinerari.forEach { destinazioneIt ->
                        if (destinazioneIt.idItinerario != idItinerarioSorgente) {
                            Text(
                                text = "➔ ${destinazioneIt.nome}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !disabilitato) {
                                        val idViaggioSicuro = viaggio.id
                                        val idDestinazioneSicura = destinazioneIt.idItinerario
                                        if (idViaggioSicuro != null && idDestinazioneSicura != null) {
                                            viewModel.menuSpostaAperto[chiaveMenu] = false
                                            viewModel.spostaViaggioItinerario(idSorgente = idItinerarioSorgente, idDestinazione = idDestinazioneSicura, idViaggio = idViaggioSicuro)
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostraConfermaRimuovi) {
        AlertDialog(
            onDismissRequest = { mostraConfermaRimuovi = false },
            title = { Text("Rimuovi Viaggio") },
            text = { Text("Vuoi davvero togliere il viaggio \"${viaggio.titolo}\" da questo itinerario?") },
            confirmButton = {
                Button(
                    onClick = {
                        viaggio.id?.let { viewModel.rimuoviViaggioDaLista(idItinerarioSorgente, it) }
                        mostraConfermaRimuovi = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Rimuovi") }
            },
            dismissButton = { TextButton(onClick = { mostraConfermaRimuovi = false }) { Text("Annulla") } }
        )
    }
}