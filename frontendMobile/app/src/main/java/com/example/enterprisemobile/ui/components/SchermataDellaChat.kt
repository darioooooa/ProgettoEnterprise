package com.example.enterprisemobile.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.enterprisemobile.ui.theme.SuccessGreen
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.enterprisemobile.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
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

    val listState = rememberLazyListState()

    LaunchedEffect(identificativoDellaStanza) {
        modelloDiVistaChat.entraNellaStanza(identificativoDellaStanza)
    }

    // Raggruppamento dei messaggi per giorno
    val messaggiRaggruppatiPerGiorno = remember(listaDeiMessaggiAttuali) {
        listaDeiMessaggiAttuali.groupBy { estraiDataFormattata(it.dataDiSpedizione) }
    }

    // Posizionamento sul fondo al primo caricamento della stanza
    LaunchedEffect(listaDeiMessaggiAttuali) {
        if (listaDeiMessaggiAttuali.isNotEmpty()) {
            // Conta quanti elementi totali ci sono (messaggi + intestazioni dei giorni)
            val totaleElementi = messaggiRaggruppatiPerGiorno.keys.size + listaDeiMessaggiAttuali.size

            // Se la chat è appena stata aperta (si è all'indice 0) teletrasporta
            if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                listState.scrollToItem(totaleElementi - 1)
            } else {
                // Se l'utente era già dentro e arriva un nuovo messaggio live, fa l'animazione verso il basso
                listState.animateScrollToItem(totaleElementi - 1)
            }
        }
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
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            messaggiRaggruppatiPerGiorno.forEach { (dataGiorno, messaggiDelGiorno) ->

                // Rimane fisso in alto finché non subentra il giorno successivo
                stickyHeader(key = dataGiorno) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background) // Copre i messaggi sotto mentre scorrono
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = dataGiorno,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Messaggi appartenenti a questo specifico giorno
                items(messaggiDelGiorno, key = { it.identificativoUnivoco ?: it.hashCode() }) { singoloMessaggio ->

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
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = singoloMessaggio.contenutoDelMessaggio,
                                    color = if (eMioMessaggio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = estraiOraFormattata(singoloMessaggio.dataDiSpedizione),
                                    fontSize = 10.sp,
                                    color = if (eMioMessaggio) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
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

//Verifica se tra il messaggio corrente e quello precedente è passato un giorno.
fun isNuovoGiorno(msgCorrente: MessaggioChatDTO?, msgPrecedente: MessaggioChatDTO?): Boolean {
    if (msgCorrente == null) return false
    if (msgPrecedente == null) return true

    val dataC = ottieniOggettoData(msgCorrente.dataDiSpedizione) ?: return false
    val dataP = ottieniOggettoData(msgPrecedente.dataDiSpedizione) ?: return true

    val calC = Calendar.getInstance().apply { time = dataC }
    val calP = Calendar.getInstance().apply { time = dataP }

    return calC.get(Calendar.YEAR) != calP.get(Calendar.YEAR) ||
            calC.get(Calendar.DAY_OF_YEAR) != calP.get(Calendar.DAY_OF_YEAR)
}

// Converte il campo data in un oggetto java.util.Date
fun ottieniOggettoData(dataObj: Any?): Date? {
    if (dataObj == null) return null
    if (dataObj is Date) return dataObj

    // Se è una stringa ISO
    if (dataObj is String) {
        return try {
            // Gestisce formati standard ISO del tipo:"2026-07-09T11:58:00"
            val formatoIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            formatoIso.parse(dataObj)
        } catch (e: Exception) {
            null
        }
    }

    // Se dal backend arriva come array
    if (dataObj is List<*>) {
        try {
            val anno = (dataObj.getOrNull(0) as? Number)?.toInt() ?: 2026
            val mese = (dataObj.getOrNull(1) as? Number)?.toInt() ?: 1
            val giorno = (dataObj.getOrNull(2) as? Number)?.toInt() ?: 1
            val ora = (dataObj.getOrNull(3) as? Number)?.toInt() ?: 0
            val minuto = (dataObj.getOrNull(4) as? Number)?.toInt() ?: 0
            val secondo = (dataObj.getOrNull(5) as? Number)?.toInt() ?: 0

            val calendar = Calendar.getInstance()
            calendar.set(anno, mese - 1, giorno, ora, minuto, secondo)
            return calendar.time
        } catch (e: Exception) {
            return null
        }
    }
    return null
}

// Estrae e formatta la data come "giovedì 09 luglio 2026"
fun estraiDataFormattata(dataObj: Any?): String {
    val data = ottieniOggettoData(dataObj) ?: return ""
    val formattaGiorno = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.ITALIAN)
    return formattaGiorno.format(data)
}

// Estrae e formatta l'ora come "11:58"
fun estraiOraFormattata(dataObj: Any?): String {
    val data = ottieniOggettoData(dataObj) ?: return ""
    val formattaOra = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formattaOra.format(data)
}