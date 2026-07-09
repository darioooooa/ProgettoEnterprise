package com.example.enterprisemobile.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.enterprisemobile.viewmodels.ChatViewModel
import com.example.enterprisemobile.ui.theme.SuccessGreen
@Composable
fun SchermataDellaChat(
    modelloDiVistaChat: ChatViewModel,
    identificativoDellaStanza: Long,
    nomeDelMittenteLocale: String,
    seiOrganizzatore: Boolean = false,
    identificativoUtenteLocale: Long = 0L
) {
    val listaDeiMessaggiAttuali by modelloDiVistaChat.messaggiVisibili.collectAsState()
    var testoDelNuovoMessaggio by remember { mutableStateOf("") }

    var mostraFinestraSegnalazione by remember { mutableStateOf(false) }
    var identificativoMessaggioDaSegnalare by remember { mutableStateOf(0L) }

    LaunchedEffect(identificativoDellaStanza) {
        modelloDiVistaChat.entraNellaStanza(identificativoDellaStanza)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        //Frase informativa per segnalazione messaggi
        if (seiOrganizzatore) {
            Text(
                text = "ℹ️ Tieni premuto su un messaggio per segnalarlo",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        modelloDiVistaChat.messaggioAvviso?.let { avviso ->

            val colore =
                if (modelloDiVistaChat.tipoAvviso == "successo")
                    SuccessGreen
                else
                    MaterialTheme.colorScheme.error

            Surface(
                color = colore.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {

                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = avviso,
                        color = colore,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "×",
                        color = colore,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            modelloDiVistaChat.azzeraMessaggioAvviso()
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaDeiMessaggiAttuali) { singoloMessaggio ->

                val eMioMessaggio = singoloMessaggio.nomeDelMittente == nomeDelMittenteLocale

                val modificatoreTocco = if (seiOrganizzatore && !eMioMessaggio) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                singoloMessaggio.identificativoUnivoco?.let { idUnivoco ->
                                    identificativoMessaggioDaSegnalare = idUnivoco
                                    mostraFinestraSegnalazione = true
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (eMioMessaggio) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (eMioMessaggio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .then(modificatoreTocco)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!eMioMessaggio) {
                                Text(
                                    text = singoloMessaggio.nomeDelMittente,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = singoloMessaggio.contenutoDelMessaggio,
                                color = if (eMioMessaggio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = testoDelNuovoMessaggio,
                onValueChange = { nuovoTesto -> testoDelNuovoMessaggio = nuovoTesto },
                modifier = Modifier.weight(1f),
                label = { Text("Scrivi il tuo messaggio qui...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (testoDelNuovoMessaggio.isNotBlank()) {
                        modelloDiVistaChat.inviaIlTuoMessaggio(
                            identificativoStanza = identificativoDellaStanza,
                            nomeMittente = nomeDelMittenteLocale,
                            testoMessaggio = testoDelNuovoMessaggio
                        )
                        testoDelNuovoMessaggio = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Invia")
            }
        }
    }

    // Dialog di segnalazione con stile scuro e callback corrette
    FinestraDiSegnalazione(
        mostraModale = mostraFinestraSegnalazione,
        suChiudi = { mostraFinestraSegnalazione = false },
        suConferma = { motivoSelezionato, descrizioneAggiuntiva ->
            modelloDiVistaChat.inviaSegnalazioneMessaggio(
                identificativoMessaggio = identificativoMessaggioDaSegnalare,
                idUtenteSegnalatore = identificativoUtenteLocale,
                motivo = motivoSelezionato,
                descrizione = descrizioneAggiuntiva,
                onSuccess = {
                    // Segnalazione inviata con successo
                    mostraFinestraSegnalazione = false
                },
                onError = { messaggio ->
                    // Errore nell'invio - potresti mostrare un Toast qui
                    mostraFinestraSegnalazione = false
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinestraDiSegnalazione(
    mostraModale: Boolean,
    suChiudi: () -> Unit,
    suConferma: (motivo: String, descrizione: String) -> Unit
) {
    if (mostraModale) {
        var motivazione by remember { mutableStateOf("") }
        var dettagli by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { suChiudi() }
        ) {
            Surface(
                color = Color(0xFF1E1E2E),
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
                            text = "🚩 Segnala messaggio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        IconButton(
                            onClick = { suChiudi() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Chiudi",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Aiutaci a mantenere la piattaforma sicura. Seleziona il motivo per cui stai segnalando questo messaggio.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Motivo della segnalazione *",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = when (motivazione) {
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
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF1E1E2E))
                        ) {
                            listOf(
                                "SPAM" to "Spam o Truffa",
                                "COMPORTAMENTO_SCORRETTO" to "Comportamento Scorretto",
                                "FALSO" to "Contenuto Falso / Inappropriato",
                                "ALTRO" to "Altro"
                            ).forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = Color.White) },
                                    onClick = {
                                        motivazione = value
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dettagli aggiuntivi (opzionale)",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dettagli,
                        onValueChange = { dettagli = it },
                        placeholder = {
                            Text(
                                "Scrivi qui i dettagli per aiutare gli amministratori...",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { suChiudi() }
                        ) {
                            Text("Annulla", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                suConferma(motivazione, dettagli)
                            },
                            enabled = motivazione.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (motivazione.isNotBlank())
                                    Color(0xFFE63946)
                                else
                                    Color.Gray
                            )
                        ) {
                            Text(
                                "Invia Segnalazione",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}