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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.Close
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

    LaunchedEffect(Unit) {
        viewModel.impostaStatiSegnalazioni(false)
    }

    val segnalazioniFiltrate = segnalazioni
        .filter { filtroTipo == null || it.tipo == filtroTipo }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Centro Segnalazioni", color = WhiteText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Totale: $totaleElementi segnalazioni", color = Color.Gray, fontSize = 12.sp)

        // Toggle Archivio/Da Gestire
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    mostraArchivio = false
                    viewModel.impostaStatiSegnalazioni(false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (!mostraArchivio) AccentBlue else Color.Gray),
                modifier = Modifier.weight(1f)
            ) { Text("Da Gestire", color = if (!mostraArchivio) DarkNavy else WhiteText, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    mostraArchivio = true
                    viewModel.impostaStatiSegnalazioni(true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (mostraArchivio) AccentBlue else Color.Gray),
                modifier = Modifier.weight(1f)
            ) { Text("Archivio", color = if (mostraArchivio) DarkNavy else WhiteText, fontWeight = FontWeight.Bold) }
        }

        // Filtro tipo + ricerca username
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var expanded by rememberSaveable { mutableStateOf(false) }
            // FILTRO TIPO STILIZZATO CON FRECCIA
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WhiteText,
                        containerColor = CardOverlay
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (expanded) AccentBlue else Color.Gray)
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
                            color = WhiteText,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Apri menu",
                            tint = AccentBlue,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(CardOverlay)
                        .padding(4.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tutti i tipi", color = WhiteText) },
                        onClick = {
                            filtroTipo = null
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo(null)
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (filtroTipo == null) AccentBlue else WhiteText
                        )
                    )
                    DropdownMenuItem(
                        text = { Text("Utenti", color = WhiteText) },
                        onClick = {
                            filtroTipo = "UTENTE"
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo("UTENTE")
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (filtroTipo == "UTENTE") AccentBlue else WhiteText
                        )
                    )
                    DropdownMenuItem(
                        text = { Text("Messaggi", color = WhiteText) },
                        onClick = {
                            filtroTipo = "MESSAGGIO"
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo("MESSAGGIO")
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (filtroTipo == "MESSAGGIO") AccentBlue else WhiteText
                        )
                    )
                    DropdownMenuItem(
                        text = { Text("Recensioni", color = WhiteText) },
                        onClick = {
                            filtroTipo = "RECENSIONE"
                            expanded = false
                            viewModel.filtraSegnalazioniPerTipo("RECENSIONE")
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (filtroTipo == "RECENSIONE") AccentBlue else WhiteText
                        )
                    )
                }
            }

            // Barra ricerca
            OutlinedTextField(
                value = queryRicerca,
                onValueChange = { queryRicerca = it },
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("Cerca username...", color = Color.Gray, fontSize = 12.sp) },
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
                        Icon(Icons.Default.Search, "Cerca", tint = AccentBlue)
                    }
                },
                trailingIcon = {
                    if (queryRicerca.isNotEmpty()) {
                        IconButton(onClick = {
                            queryRicerca = ""
                            viewModel.cercaSegnalazioniPerUsername("")
                        }) {
                            Icon(Icons.Default.Clear, "Cancella", tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingSegnalazioni && segnalazioni.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (segnalazioniFiltrate.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (queryRicerca.isNotBlank()) "Nessun risultato per \"$queryRicerca\""
                    else if (mostraArchivio) "Nessuna segnalazione in archivio."
                    else "Nessuna segnalazione da gestire.",
                    color = Color.Gray
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

                // ✅ PAGINAZIONE: Mostra sempre se ci sono elementi
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
                                    containerColor = if (paginaCorrente > 0) AccentBlue else Color.Gray
                                )
                            ) { Text("Prec", fontSize = 14.sp) }

                            Text(
                                text = "Pagina ${paginaCorrente + 1} di $totalePagine",
                                color = WhiteText,
                                fontWeight = FontWeight.Medium
                            )

                            Button(
                                onClick = { viewModel.paginaSuccessivaSegnalazioni() },
                                enabled = paginaCorrente < totalePagine - 1,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paginaCorrente < totalePagine - 1) AccentBlue else Color.Gray
                                )
                            ) { Text("Succ", fontSize = 14.sp) }
                        }
                    }
                }
            }
        }
    }

    // Dialog Risolvi
    if (mostraModaleRisolvi) {
        AlertDialog(
            onDismissRequest = { mostraModaleRisolvi = false },
            title = { Text("Conferma Risoluzione", color = WhiteText) },
            text = { Text("Confermi la risoluzione della segnalazione (con eventuale rimozione dell'elemento)?", color = Color.LightGray) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleRisolvi = false }) { Text("Annulla", color = Color.Gray) } },
            containerColor = DarkNavy
        )
    }

    // Dialog Banna
    if (mostraModaleBanna) {
        AlertDialog(
            onDismissRequest = { mostraModaleBanna = false },
            title = { Text("⚠ ATTENZIONE BAN", color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text("Stai per applicare la sanzione più dura sull'elemento e sospendere definitivamente l'utente. Confermi?", color = Color.LightGray) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Sì, Banna Utente") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleBanna = false }) { Text("Annulla", color = Color.Gray) } },
            containerColor = DarkNavy
        )
    }

    // Dialog Rifiuta
    if (mostraModaleRifiuta) {
        AlertDialog(
            onDismissRequest = { mostraModaleRifiuta = false },
            title = { Text("❌ Rifiuta Segnalazione", color = Color.Gray, fontWeight = FontWeight.Bold) },
            text = { Text("Rifiutando questa segnalazione, l'elemento segnalato NON verrà rimosso e l'utente non riceverà alcuna sanzione. Confermi?", color = Color.LightGray) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Rifiuta", color = WhiteText, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostraModaleRifiuta = false }) { Text("Annulla", color = Color.Gray) } },
            containerColor = DarkNavy
        )
    }

    // Dialog Contenuto
    if (mostraModaleContenuto) {
        AlertDialog(
            onDismissRequest = { mostraModaleContenuto = false },
            title = { Text("📄 Dettagli Contenuto", color = AccentBlue, fontWeight = FontWeight.Bold) },
            text = {
                Surface(
                    color = DarkNavy.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = contenutoDaVisualizzare,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostraModaleContenuto = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("Chiudi", color = DarkNavy, fontWeight = FontWeight.Bold) }
            },
            containerColor = DarkNavy
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
        color = CardOverlay,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().animateContentSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${segnalazione.id} - ${segnalazione.tipo}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Inviata da: ${segnalazione.segnalatoreUsername ?: "N/D"}", color = Color.LightGray, fontSize = 12.sp)
                }

                val (statoColore, statoTesto) = when (segnalazione.stato.toString()) {
                    "APERTA" -> Color(0xFFF59E0B) to "APERTA"
                    "IN_LAVORAZIONE" -> AccentBlue to "IN LAVORAZIONE"
                    "CHIUSA" -> SuccessGreen to "RISOLTA"
                    "RIFIUTATA" -> DangerRed to "RIFIUTATA"
                    else -> Color.Gray to segnalazione.stato.toString()
                }

                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statoColore.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text(statoTesto, color = statoColore, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                Icon(
                    Icons.Default.KeyboardArrowDown,
                    null,
                    tint = Color.Gray,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            // Dettagli espansi
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Motivo: ${segnalazione.motivo}", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = segnalazione.descrizione ?: "Nessuna descrizione", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // "Leggi Contenuto" SOLO per MESSAGGIO e RECENSIONE
                if (segnalazione.tipo == "MESSAGGIO" || segnalazione.tipo == "RECENSIONE") {
                    OutlinedButton(
                        onClick = onVediContenuto,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
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
                    Button(onClick = onPrendiInCarico, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), modifier = Modifier.fillMaxWidth()) {
                        Text("Prendi in carico", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                } else if (segnalazione.stato.toString() == "IN_LAVORAZIONE") {
                    when (segnalazione.tipo) {
                        "MESSAGGIO" -> {
                            Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)), modifier = Modifier.fillMaxWidth()) {
                                Text("🗑️ Elimina Messaggio", color = WhiteText, fontWeight = FontWeight.Bold)
                            }
                        }
                        "RECENSIONE" -> {
                            Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)), modifier = Modifier.fillMaxWidth()) {
                                Text("🗑️ Elimina Recensione", color = WhiteText, fontWeight = FontWeight.Bold)
                            }
                        }
                        "UTENTE" -> {
                            Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308)), modifier = Modifier.fillMaxWidth()) {
                                Text("⚠️ Segnala Utente", color = DarkNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onBanna, colors = ButtonDefaults.buttonColors(containerColor = DangerRed), modifier = Modifier.weight(1f)) {
                            Text("⛔ Banna", fontSize = 12.sp)
                        }
                        Button(onClick = onRifiuta, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                            Text("Rifiuta", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Text("Segnalazione archiviata", color = Color.Gray, fontSize = 12.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}