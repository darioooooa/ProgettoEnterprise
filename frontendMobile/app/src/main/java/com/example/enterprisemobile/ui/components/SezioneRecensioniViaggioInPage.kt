package com.example.enterprisemobile.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.enterprisemobile.viewmodels.CommunityViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SezioneRecensioniViaggioInPage(
    viewModel: CommunityViewModel,
    viaggioId: Long,
    isMioViaggio: Boolean,
    isGiaAcquistato: Boolean,
    statoSvolgimentoIscrizione: String,
    context: Context
) {
    val recensioni by viewModel.recensioni.collectAsState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    LaunchedEffect(viaggioId) {
        if (viaggioId != -1L) {
            viewModel.caricaRecensioni(viaggioId)
        }
    }

    var dDaAperto by remember { mutableStateOf(false) }
    var dAAperto by remember { mutableStateOf(false) }
    var filtriEspansi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Recensioni della community",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Leggi i commenti degli altri viaggiatori o lascia la tua valutazione",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        viewModel.messaggioAvviso?.let { avviso ->
            val colore = if (viewModel.tipoAvviso == "successo") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filtriEspansi = !filtriEspansi },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtra recensioni",
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
                            OutlinedTextField(value = viewModel.filtroParolaChiave, onValueChange = { viewModel.filtroParolaChiave = it }, label = { Text("Parola chiave") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            OutlinedTextField(
                                value = viewModel.filtroVotoEsatto,
                                onValueChange = { if (it.isEmpty() || it.trim() in listOf("1", "2", "3", "4", "5")) viewModel.filtroVotoEsatto = it.trim() },
                                label = { Text("Voto (1-5)") },
                                modifier = Modifier.weight(1f),
                                colors = textFieldColors
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = viewModel.filtroDataDa, onValueChange = {}, label = { Text("Da data") }, readOnly = true, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                                Box(modifier = Modifier.matchParentSize().clickable { dDaAperto = true })
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = viewModel.filtroDataA, onValueChange = {}, label = { Text("A data") }, readOnly = true, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                                Box(modifier = Modifier.matchParentSize().clickable { dAAperto = true })
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { viewModel.pulisciFiltriRecensioni(viaggioId) }) {
                                Text("Resetta", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.filtraRecensioni(viaggioId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Cerca")
                            }
                        }
                    }
                }
            }
        }

        val haPartecipatoEIniziato = isGiaAcquistato && (statoSvolgimentoIscrizione == "IN_CORSO" || statoSvolgimentoIscrizione == "COMPLETATO")
        val mostraFormScrittura = !viewModel.haGiaRecensito || viewModel.inModifica

        if (!isMioViaggio && viewModel.mioRuolo == "ROLE_VIAGGIATORE" && haPartecipatoEIniziato) {
            if (mostraFormScrittura) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (viewModel.inModifica) "✏️ Modifica la tua recensione" else "💬 Lascia una recensione per questo viaggio",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Valutazione: ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            (1..5).forEach { stella ->
                                val selezionata = viewModel.votoInput >= stella
                                Text(
                                    text = "★",
                                    color = if (selezionata) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outline,
                                    fontSize = 24.sp,
                                    modifier = Modifier.clickable { viewModel.votoInput = stella }
                                )
                            }
                        }

                        OutlinedTextField(value = viewModel.commentoInput, onValueChange = { viewModel.commentoInput = it }, label = { Text("Scrivi il tuo commento...") }, modifier = Modifier.fillMaxWidth(), minLines = 2, colors = textFieldColors)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.inModifica) {
                                TextButton(onClick = { viewModel.pulisciForm() }) {
                                    Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Button(
                                onClick = { viewModel.aggiungiRecensione(context, viaggioId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (viewModel.inModifica) "Aggiorna" else "Invia recensione")
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("✅", fontSize = 20.sp)
                        Text(text = "Grazie per aver valutato questo viaggio! Trovi la tua recensione nell'elenco della community.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }

        if (recensioni.isEmpty()) {
            Text(
                text = "Nessuna recensione corrispondente ai criteri impostati.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            recensioni.forEach { rec ->
                val miaRecensione = rec.utenteUsername == viewModel.mioUsername

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = if (miaRecensione) "Tu (${rec.utenteUsername ?: "Anonimo"})" else rec.utenteUsername ?: "Utente Anonimo", color = if (miaRecensione) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = rec.dataCreazione?.replace("T", " ")?.take(10) ?: "Data non disponibile", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                            Row {
                                (1..5).forEach { s ->
                                    Text(text = "★", color = if (rec.voto >= s) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outline, fontSize = 16.sp)
                                }
                            }
                        }

                        if (!rec.commento.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(rec.commento ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 19.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                            if (miaRecensione) {
                                Text(text = "Modifica", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.avviaModifica(rec) }.padding(end = 16.dp))
                            }

                            if (!miaRecensione) {
                                Text(
                                    text = "🚩 Segnala",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { viewModel.apriDialogSegnalazioneRecensione(rec) }
                                        .padding(end = 16.dp)
                                )
                            }

                            Text(
                                text = if (viewModel.idRecensioneDaEliminare == rec.id) "⚠️ Confermi?" else "Elimina",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { viewModel.cancellaRecensione(context, viaggioId, rec.id) }
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.totalePagineRecensioni > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.cambiaPagina(viaggioId, -1) },
                    enabled = viewModel.paginaRecensioni > 0
                ) {
                    Text("←", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "${viewModel.paginaRecensioni + 1} di ${viewModel.totalePagineRecensioni}",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = { viewModel.cambiaPagina(viaggioId, 1) },
                    enabled = viewModel.paginaRecensioni < viewModel.totalePagineRecensioni - 1
                ) {
                    Text("→", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (viewModel.showSegnalazioneRecensioneDialog && viewModel.recensioneDaSegnalare != null) {
        DialogSegnalazioneRecensione(
            username = viewModel.recensioneDaSegnalare!!.utenteUsername ?: "Utente",
            isLoading = viewModel.isLoadingSegnalazione,
            onDismiss = { viewModel.chiudiDialogSegnalazioneRecensione() },
            onInvia = { motivo, descrizione ->
                viewModel.inviaSegnalazioneRecensione(
                    context = context,
                    motivo = motivo,
                    descrizione = descrizione,
                    onSuccess = {
                        Toast.makeText(context, "Segnalazione inviata!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (dDaAperto) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dDaAperto = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val data = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.filtroDataDa = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    dDaAperto = false
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dDaAperto = false }) { Text("Annulla") } }
        ) { DatePicker(state = state) }
    }

    if (dAAperto) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { dAAperto = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val data = Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.filtroDataA = data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    dAAperto = false
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dAAperto = false }) { Text("Annulla") } }
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogSegnalazioneRecensione(
    username: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onInvia: (String, String) -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    var descrizione by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚩 Segnala recensione",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(
                        onClick = {
                            if (!isLoading) onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "di $username",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Aiutaci a mantenere la piattaforma sicura. Seleziona il motivo per cui stai segnalando questa recensione.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Motivo della segnalazione *",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = when (motivo) {
                            "SPAM" -> "Spam o Truffa"
                            "COMPORTAMENTO_SCORRETTO" -> "Comportamento Scorretto"
                            "FALSO" -> "Contenuto Falso / Inappropriato"
                            "ALTRO" -> "Altro"
                            else -> "-- Seleziona un motivo --"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        listOf(
                            "SPAM" to "Spam o Truffa",
                            "COMPORTAMENTO_SCORRETTO" to "Comportamento Scorretto",
                            "FALSO" to "Contenuto Falso / Inappropriato",
                            "ALTRO" to "Altro"
                        ).forEach { (value, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(label, color = MaterialTheme.colorScheme.onSurface)
                                },
                                onClick = {
                                    motivo = value
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dettagli aggiuntivi (opzionale)",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descrizione,
                    onValueChange = { descrizione = it },
                    placeholder = {
                        Text(
                            "Scrivi qui i dettagli per aiutare gli amministratori...",
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Annulla", color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onInvia(motivo, descrizione)
                        },
                        enabled = motivo.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (motivo.isNotBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (motivo.isNotBlank()) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            Text("Invia Segnalazione")
                        }
                    }
                }
            }
        }
    }
}