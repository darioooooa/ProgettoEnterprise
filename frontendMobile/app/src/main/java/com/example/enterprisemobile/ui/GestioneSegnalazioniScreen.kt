package com.example.enterprisemobile.ui

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.model.SegnalazioneDTO
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.AdminViewModel

@Composable
fun GestioneSegnalazioniScreen(viewModel: AdminViewModel) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val adminId = sessionManager.ottieniIdUtente()?.toLongOrNull() ?: 0L

    val segnalazioni by viewModel.segnalazioni.observeAsState(emptyList())
    val isLoadingSegnalazioni by viewModel.isLoadingSegnalazioni.observeAsState(false)

    // Stati paginazione
    val paginaCorrente by viewModel.paginaCorrenteSegnalazioni.observeAsState(0)
    val totalePagine by viewModel.totalePagineSegnalazioni.observeAsState(0)
    val totaleElementi by viewModel.totaleElementiSegnalazioni.observeAsState(0)

    var mostraArchivio by rememberSaveable { mutableStateOf(false) }
    var filtroTipo by rememberSaveable { mutableStateOf<String?>(null) }
    var queryRicerca by rememberSaveable { mutableStateOf("") }
    var mostraModaleRisolvi by rememberSaveable { mutableStateOf(false) }
    var mostraModaleBanna by rememberSaveable { mutableStateOf(false) }
    var mostraModaleRifiuta by rememberSaveable { mutableStateOf(false) }
    var mostraModaleContenuto by rememberSaveable { mutableStateOf(false) }
    var segnalazioneSelezionata by rememberSaveable { mutableStateOf<Long?>(null) }
    var contenutoDaVisualizzare by rememberSaveable { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    LaunchedEffect(Unit) {
        viewModel.impostaStatiSegnalazioni(false)
    }

    val segnalazioniFiltrate = segnalazioni
        .filter { filtroTipo == null || it.tipo == filtroTipo }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 1f))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Centro Segnalazioni", color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Totale: $totaleElementi segnalazioni", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)

        // Toggle archivio / da gestire
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    mostraArchivio = false
                    viewModel.impostaStatiSegnalazioni(false)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!mostraArchivio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!mostraArchivio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Da Gestire", fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    mostraArchivio = true
                    viewModel.impostaStatiSegnalazioni(true)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mostraArchivio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (mostraArchivio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Archivio", fontWeight = FontWeight.Bold) }
        }

        // Filtro tipo + ricerca username
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var expanded by rememberSaveable { mutableStateOf(false) }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (filtroTipo) {
                                "UTENTE" -> "Utenti"
                                "MESSAGGIO" -> "Messaggi"
                                "RECENSIONE" -> "Recensioni"
                                else -> "Tutti i tipi"
                            },
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Apri menu",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tutti i tipi", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            filtroTipo = null
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo(null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Utenti", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            filtroTipo = "UTENTE"
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo("UTENTE")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Messaggi", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            filtroTipo = "MESSAGGIO"
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo("MESSAGGIO")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Recensioni", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            filtroTipo = "RECENSIONE"
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo("RECENSIONE")
                        }
                    )
                }
            }

            // Barra ricerca
            OutlinedTextField(
                value = queryRicerca,
                onValueChange = { queryRicerca = it },
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("Cerca username...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.cercaSegnalazioniPerUsername(queryRicerca)
                    }
                ),
                leadingIcon = {
                    IconButton(onClick = {
                        viewModel.cercaSegnalazioniPerUsername(queryRicerca)
                    }) {
                        Icon(Icons.Default.Search, "Cerca", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                trailingIcon = {
                    if (queryRicerca.isNotEmpty()) {
                        IconButton(onClick = {
                            queryRicerca = ""
                            viewModel.cercaSegnalazioniPerUsername("")
                        }) {
                            Icon(Icons.Default.Clear, "Cancella", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                colors = textFieldColors
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingSegnalazioni && segnalazioni.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (segnalazioniFiltrate.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (queryRicerca.isNotBlank()) "Nessun risultato per \"$queryRicerca\""
                    else if (mostraArchivio) "Nessuna segnalazione in archivio."
                    else "Nessuna segnalazione da gestire.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(segnalazioniFiltrate, key = { it.id }) { segnalazione ->
                    CartaSegnalazione(
                        segnalazione = segnalazione,
                        mostraAzioni = !mostraArchivio,
                        onPrendiInCarico = {
                            viewModel.prendiInCaricoSegnalazione(segnalazione.id.toLong(), adminId,
                                onSuccess = {
                                    Toast.makeText(context, "Presa in carico!", Toast.LENGTH_SHORT).show()
                                    viewModel.caricaSegnalazioni()
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        onRisolvi = {
                            segnalazioneSelezionata = segnalazione.id.toLong()
                            mostraModaleRisolvi = true
                        },
                        onBanna = {
                            segnalazioneSelezionata = segnalazione.id.toLong()
                            mostraModaleBanna = true
                        },
                        onRifiuta = {
                            segnalazioneSelezionata = segnalazione.id.toLong()
                            mostraModaleRifiuta = true
                        },
                        onVediContenuto = {
                            contenutoDaVisualizzare = segnalazione.riferimentoNome ?: "Contenuto non disponibile"
                            mostraModaleContenuto = true
                        }
                    )
                }

                // Paginazione: mostra sempre se ci sono elementi
                if (totaleElementi > 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.paginaPrecedenteSegnalazioni() },
                                enabled = paginaCorrente > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paginaCorrente > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (paginaCorrente > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) { Text("Prec", fontSize = 14.sp) }

                            Text(
                                text = "Pagina ${paginaCorrente + 1} di $totalePagine",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium
                            )

                            Button(
                                onClick = { viewModel.paginaSuccessivaSegnalazioni() },
                                enabled = paginaCorrente < totalePagine - 1,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paginaCorrente < totalePagine - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (paginaCorrente < totalePagine - 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) { Text("Succ", fontSize = 14.sp) }
                        }
                    }
                }
            }
        }
    }

    // Dialog risolvi
    if (mostraModaleRisolvi) {
        AlertDialog(
            onDismissRequest = { mostraModaleRisolvi = false },
            title = { Text("Conferma Risoluzione", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Confermi la risoluzione della segnalazione (con eventuale rimozione dell'elemento)?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        segnalazioneSelezionata?.let { id ->
                            viewModel.risolviSegnalazione(id, adminId, false,
                                onSuccess = {
                                    Toast.makeText(context, "Segnalazione risolta!", Toast.LENGTH_SHORT).show()
                                    mostraModaleRisolvi = false
                                    viewModel.caricaSegnalazioni()
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleRisolvi = false }) { Text("Annulla", color = MaterialTheme.colorScheme.outline) } },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // Dialog banna
    if (mostraModaleBanna) {
        AlertDialog(
            onDismissRequest = { mostraModaleBanna = false },
            title = { Text("⚠ ATTENZIONE BAN", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = { Text("Stai per applicare la sanzione più dura sull'elemento e sospendere definitivamente l'utente. Confermi?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        segnalazioneSelezionata?.let { id ->
                            viewModel.risolviSegnalazione(id, adminId, true,
                                onSuccess = {
                                    Toast.makeText(context, "Utente bannato con successo!", Toast.LENGTH_LONG).show()
                                    mostraModaleBanna = false
                                    viewModel.caricaSegnalazioni()
                                    viewModel.caricaUtentiBannati()
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) { Text("Sì, Banna Utente") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleBanna = false }) { Text("Annulla", color = MaterialTheme.colorScheme.outline) } },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // Dialog rifiuta
    if (mostraModaleRifiuta) {
        AlertDialog(
            onDismissRequest = { mostraModaleRifiuta = false },
            title = { Text("❌ Rifiuta Segnalazione", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Rifiutando questa segnalazione, l'elemento segnalato NON verrà rimosso e l'utente non riceverà alcuna sanzione. Confermi?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        segnalazioneSelezionata?.let { id ->
                            viewModel.rifiutaSegnalazione(id, adminId,
                                onSuccess = {
                                    Toast.makeText(context, "Segnalazione rifiutata.", Toast.LENGTH_SHORT).show()
                                    mostraModaleRifiuta = false
                                    viewModel.caricaSegnalazioni()
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text("Rifiuta", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostraModaleRifiuta = false }) { Text("Annulla", color = MaterialTheme.colorScheme.outline) } },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // Dialog Contenuto
    if (mostraModaleContenuto) {
        AlertDialog(
            onDismissRequest = { mostraModaleContenuto = false },
            title = { Text("📄 Dettagli Contenuto", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = contenutoDaVisualizzare,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostraModaleContenuto = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text("Chiudi", fontWeight = FontWeight.Bold) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
fun CartaSegnalazione(
    segnalazione: SegnalazioneDTO,
    mostraAzioni: Boolean,
    onPrendiInCarico: () -> Unit,
    onRisolvi: () -> Unit,
    onBanna: () -> Unit,
    onRifiuta: () -> Unit,
    onVediContenuto: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().animateContentSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${segnalazione.id} - ${segnalazione.tipo}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Inviata da: ${segnalazione.segnalatoreUsername ?: "N/D"}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
                }

                val (statoColore, statoTesto) = when (segnalazione.stato.toString()) {
                    "APERTA" -> MaterialTheme.colorScheme.tertiary to "APERTA"
                    "IN_LAVORAZIONE" -> MaterialTheme.colorScheme.primary to "IN LAVORAZIONE"
                    "CHIUSA" -> MaterialTheme.colorScheme.primary to "RISOLTA"
                    "RIFIUTATA" -> MaterialTheme.colorScheme.error to "RIFIUTATA"
                    else -> MaterialTheme.colorScheme.outline to segnalazione.stato.toString()
                }

                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statoColore.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text(statoTesto, color = statoColore, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                Icon(
                    Icons.Default.KeyboardArrowDown,
                    null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            // Dettagli espansi
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Motivo: ${segnalazione.motivo}", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = segnalazione.descrizione ?: "Nessuna descrizione", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (segnalazione.tipo == "MESSAGGIO" || segnalazione.tipo == "RECENSIONE") {
                    OutlinedButton(
                        onClick = onVediContenuto,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Description, "Leggi contenuto", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leggi Contenuto", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Azioni
            if (mostraAzioni) {
                if (segnalazione.stato.toString() == "APERTA") {
                    Button(
                        onClick = onPrendiInCarico,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Prendi in carico", fontWeight = FontWeight.Bold)
                    }
                } else if (segnalazione.stato.toString() == "IN_LAVORAZIONE") {
                    when (segnalazione.tipo) {
                        "MESSAGGIO" -> {
                            Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError), modifier = Modifier.fillMaxWidth()) {
                                Text("🗑️ Elimina Messaggio", fontWeight = FontWeight.Bold)
                            }
                        }
                        "RECENSIONE" -> {
                            Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError), modifier = Modifier.fillMaxWidth()) {
                                Text("🗑️ Elimina Recensione", fontWeight = FontWeight.Bold)
                            }
                        }
                        "UTENTE" -> {
                            Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary), modifier = Modifier.fillMaxWidth()) {
                                Text("⚠️ Segnala Utente", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onBanna, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError), modifier = Modifier.weight(1f)) {
                            Text("⛔ Banna", fontSize = 12.sp)
                        }
                        Button(onClick = onRifiuta, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface), modifier = Modifier.weight(1f), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                            Text("Rifiuta", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Text("Segnalazione archiviata", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 12.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}