package com.example.enterprisemobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import com.example.enterprisemobile.viewmodels.ChatViewModel

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

    FinestraDiSegnalazione(
        mostraModale = mostraFinestraSegnalazione,
        suChiudi = { mostraFinestraSegnalazione = false },
        suConferma = { motivoSelezionato, descrizioneAggiuntiva ->
            modelloDiVistaChat.inviaSegnalazioneMessaggio(
                identificativoMessaggio = identificativoMessaggioDaSegnalare,
                idUtenteSegnalatore = identificativoUtenteLocale,
                motivo = motivoSelezionato,
                descrizione = descrizioneAggiuntiva
            )
            mostraFinestraSegnalazione = false
        }
    )
}

@Composable
fun FinestraDiSegnalazione(
    mostraModale: Boolean,
    suChiudi: () -> Unit,
    suConferma: (motivo: String, descrizione: String) -> Unit
) {
    if (mostraModale) {
        var motivazione by remember { mutableStateOf("") }
        var dettagli by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = suChiudi,
            title = { Text("Segnala Messaggio", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Perché stai segnalando questo messaggio?")

                    OutlinedTextField(
                        value = motivazione,
                        onValueChange = { motivazione = it },
                        label = { Text("Motivo (es. Spam, Offese)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dettagli,
                        onValueChange = { dettagli = it },
                        label = { Text("Dettagli aggiuntivi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { suConferma(motivazione, dettagli) },
                    enabled = motivazione.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Invia Segnalazione")
                }
            },
            dismissButton = {
                TextButton(onClick = suChiudi) { Text("Annulla") }
            }
        )
    }
}