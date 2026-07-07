package com.example.enterprisemobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.components.TopBar
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.example.enterprisemobile.model.SegnalazioneDTO
import com.example.enterprisemobile.model.UtenteBannatoDTO


class AdminActivity : ComponentActivity() {
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                AdminContent(viewModel)
            }
        }
    }
}

@Composable
fun AdminContent(viewModel: AdminViewModel) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val nomeAdmin = sessionManager.ottieniUsername() ?: "Amministratore"

    val richieste by viewModel.richiestePromozione.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val documentoScaricato by viewModel.documentoScaricato.observeAsState()
    val paginaCorrente by viewModel.paginaCorrente.observeAsState(0)
    val totalePagine by viewModel.totalePagine.observeAsState(0)
    val totaleElementi by viewModel.totaleElementi.observeAsState(0)

    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var vistaAttuale by remember { mutableStateOf("PENDENTI") }
    var isDownloading by remember { mutableStateOf(false) }

    var queryRicerca by remember { mutableStateOf("") }

    var mostraModaleMotivazione by remember { mutableStateOf(false) }
    var mostraModaleBiografia by remember { mutableStateOf(false) }
    var testoDaVisualizzare by remember { mutableStateOf("") }
    var mostraModaleRifiuto by remember { mutableStateOf(false) }
    var richiestaDaRifiutare by remember { mutableStateOf<Long?>(null) }
    var motivazioneRifiuto by remember { mutableStateOf("") }
    var mostraModaleConfermaApprova by remember { mutableStateOf(false) }
    var richiestaDaApprovare by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.filtraPerStato("IN_ATTESA")
        viewModel.caricaSegnalazioni()
        viewModel.caricaUtentiBannati()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(documentoScaricato) {
        if (documentoScaricato != null) {
            isDownloading = false
            documentoScaricato?.let { body ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        if (downloadsDir != null && !downloadsDir.exists()) {
                            downloadsDir.mkdirs()
                        }

                        val tipoContenuto = body.contentType()?.toString()?.lowercase() ?: ""
                        val estensione = if (tipoContenuto.contains("word") || tipoContenuto.contains("document")) {
                            ".docx"
                        } else {
                            ".pdf"
                        }

                        val fileName = "candidatura_${System.currentTimeMillis()}$estensione"
                        val file = File(downloadsDir, fileName)

                        FileOutputStream(file).use { outputStream ->
                            body.byteStream().copyTo(outputStream)
                        }

                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            mediaScanIntent.data = Uri.fromFile(file)
                            context.sendBroadcast(mediaScanIntent)

                            Toast.makeText(context, "✅ File salvato in: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                            apriFile(context, file)
                        }
                    } catch (e: Exception) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            Toast.makeText(context, "❌ Errore salvataggio: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            viewModel.resetDocumentoScaricato()
        }
    }

    if (isDownloading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(modifier = Modifier.padding(32.dp), colors = CardDefaults.cardColors(containerColor = CardOverlay)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Download documento...", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (mostraModaleMotivazione) {
        AlertDialog(
            onDismissRequest = { mostraModaleMotivazione = false },
            title = { Text("Motivazione della Candidatura", fontWeight = FontWeight.Bold, color = WhiteText) },
            text = { Text(testoDaVisualizzare, color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { mostraModaleMotivazione = false }) { Text("Chiudi", color = AccentBlue) }
            },
            containerColor = DarkNavy
        )
    }

    if (mostraModaleBiografia) {
        AlertDialog(
            onDismissRequest = { mostraModaleBiografia = false },
            title = { Text("Biografia Professionale", fontWeight = FontWeight.Bold, color = WhiteText) },
            text = { Text(testoDaVisualizzare, color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { mostraModaleBiografia = false }) { Text("Chiudi", color = AccentBlue) }
            },
            containerColor = DarkNavy
        )
    }

    if (mostraModaleRifiuto) {
        AlertDialog(
            onDismissRequest = { mostraModaleRifiuto = false },
            title = { Text("Rifiuta Candidatura", fontWeight = FontWeight.Bold, color = WhiteText) },
            text = {
                Column {
                    Text("Inserisci il motivo del rifiuto:", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivazioneRifiuto,
                        onValueChange = { motivazioneRifiuto = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText, unfocusedTextColor = WhiteText,
                            focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray
                        ),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        richiestaDaRifiutare?.let { id ->
                            viewModel.rifiutaRichiesta(
                                id = id, motivazione = motivazioneRifiuto,
                                onSuccess = {
                                    Toast.makeText(context, "Candidatura rifiutata", Toast.LENGTH_SHORT).show()
                                    mostraModaleRifiuto = false
                                    motivazioneRifiuto = ""
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Conferma Rifiuto", color = WhiteText) }
            },
            dismissButton = {
                TextButton(onClick = { mostraModaleRifiuto = false }) { Text("Annulla", color = Color.Gray) }
            },
            containerColor = DarkNavy
        )
    }

    if (mostraModaleConfermaApprova) {
        AlertDialog(
            onDismissRequest = { mostraModaleConfermaApprova = false },
            title = { Text("Conferma Approvazione", fontWeight = FontWeight.Bold, color = WhiteText) },
            text = {
                Text(
                    "Sei sicuro di voler approvare questa candidatura? " +
                            "L'utente verrà promosso a Organizzatore e riceverà le credenziali di accesso via email.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        richiestaDaApprovare?.let { id ->
                            viewModel.approvaRichiesta(
                                id = id,
                                adminId = 0L,
                                onSuccess = {
                                    Toast.makeText(context, "Approvata!", Toast.LENGTH_SHORT).show()
                                    mostraModaleConfermaApprova = false
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Errore: $err", Toast.LENGTH_LONG).show()
                                    mostraModaleConfermaApprova = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("✅ Conferma", color = WhiteText) }
            },
            dismissButton = {
                TextButton(onClick = { mostraModaleConfermaApprova = false }) {
                    Text("Annulla", color = Color.Gray)
                }
            },
            containerColor = DarkNavy
        )
    }

    AdminScaffold(
        titolo = "PANNELLO ADMIN",
        nomeUtente = nomeAdmin,
        bottomBar = {
            NavigationBar(containerColor = DarkNavy) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Richieste") },
                    label = { Text("Richieste", fontSize = 12.sp) },
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhiteText, unselectedIconColor = Color.Gray, indicatorColor = CardOverlay
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Warning, contentDescription = "Segnalazioni") },
                    label = { Text("Segnalazioni", fontSize = 12.sp) },
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhiteText, unselectedIconColor = Color.Gray, indicatorColor = CardOverlay
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Block, contentDescription = "Ban") },
                    label = { Text("Ban", fontSize = 12.sp) },
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhiteText, unselectedIconColor = Color.Gray, indicatorColor = CardOverlay
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(DarkNavy).padding(innerPadding)) {
            if (isLoading && richieste.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentBlue)
            } else {
                when (selectedBottomTab) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Richieste di Promozione", color = WhiteText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { vistaAttuale = "PENDENTI"; viewModel.filtraPerStato("IN_ATTESA") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vistaAttuale == "PENDENTI") AccentBlue else Color.Gray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Richieste Pendenti", fontSize = 12.sp) }
                                Button(
                                    onClick = { vistaAttuale = "STORICO"; viewModel.filtraPerStato(null) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vistaAttuale == "STORICO") AccentBlue else Color.Gray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Storico Valutazioni", fontSize = 12.sp) }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = queryRicerca,
                                onValueChange = { queryRicerca = it },
                                label = { Text("Cerca per username...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = WhiteText, unfocusedTextColor = WhiteText,
                                    focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.Gray
                                ),
                                trailingIcon = {
                                    Row {
                                        if (queryRicerca.isNotEmpty()) {
                                            IconButton(onClick = {
                                                queryRicerca = ""
                                                viewModel.cercaPerUsername("")
                                            }) {
                                                Icon(Icons.Filled.Clear, "Cancella", tint = Color.Gray)
                                            }
                                        }
                                        IconButton(onClick = {
                                            viewModel.cercaPerUsername(queryRicerca)
                                        }) {
                                            Icon(Icons.Filled.Search, "Cerca", tint = AccentBlue)
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { viewModel.cercaPerUsername(queryRicerca) }
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val richiesteFiltrate = if (vistaAttuale == "STORICO") {
                                richieste.filter { it.stato == "APPROVATA" || it.stato == "RIFIUTATA" }
                            } else {
                                richieste
                            }

                            if (richiesteFiltrate.isEmpty() && totaleElementi > 0) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Nessuna richiesta trovata in questa pagina.", color = Color.Gray)
                                }
                            } else if (richiesteFiltrate.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Nessuna richiesta trovata.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(richiesteFiltrate, key = { it.id }) { richiesta ->
                                        CartaRichiestaEspandibile(
                                            richiesta = richiesta,
                                            onApprova = {
                                                richiestaDaApprovare = richiesta.id
                                                mostraModaleConfermaApprova = true
                                            },
                                            onRifiuta = {
                                                richiestaDaRifiutare = richiesta.id
                                                motivazioneRifiuto = ""
                                                mostraModaleRifiuto = true
                                            },
                                            onScarica = {
                                                isDownloading = true
                                                viewModel.scaricaDocumento(richiesta.id)
                                            },
                                            onVediMotivazione = {
                                                testoDaVisualizzare = richiesta.motivazione
                                                mostraModaleMotivazione = true
                                            },
                                            onVediBiografia = {
                                                testoDaVisualizzare = richiesta.biografiaProfessionale
                                                mostraModaleBiografia = true
                                            }
                                        )
                                    }

                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { viewModel.paginaPrecedente() },
                                                enabled = paginaCorrente > 0,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (paginaCorrente > 0) AccentBlue else Color.Gray
                                                )
                                            ) { Text("Prec", fontSize = 14.sp) }

                                            Text(
                                                text = "Pagina ${paginaCorrente + 1} di ${totalePagine.coerceAtLeast(1)}",
                                                color = WhiteText, fontWeight = FontWeight.Medium
                                            )

                                            Button(
                                                onClick = { viewModel.paginaSuccessiva() },
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
                    1 -> SchermataSegnalazioni(viewModel)
                    2 -> SchermataUtentiBannati(viewModel)
                }
            }
        }
    }
}

@Composable
fun SchermataSegnalazioni(viewModel: AdminViewModel) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val adminId = sessionManager.ottieniIdUtente()?.toLongOrNull() ?: 0L

    val segnalazioni by viewModel.segnalazioni.observeAsState(emptyList())
    var mostraArchivio by remember { mutableStateOf(false) }

    var mostraModaleRisolvi by remember { mutableStateOf(false) }
    var mostraModaleBanna by remember { mutableStateOf(false) }
    var segnalazioneSelezionata by remember { mutableStateOf<Long?>(null) }

    val segnalazioniFiltrate = if (mostraArchivio) {
        segnalazioni.filter { it.stato.toString() == "CHIUSA" || it.stato.toString() == "RIFIUTATA" }
    } else {
        segnalazioni.filter { it.stato.toString() == "APERTA" || it.stato.toString() == "IN_LAVORAZIONE" }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Centro Segnalazioni", color = WhiteText, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { mostraArchivio = false
                          viewModel.caricaSegnalazioni(null)},
                colors = ButtonDefaults.buttonColors(containerColor = if (!mostraArchivio) AccentBlue else Color.Gray),
                modifier = Modifier.weight(1f)
            ) { Text("Da Gestire", color = if (!mostraArchivio) DarkNavy else WhiteText, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { mostraArchivio = true
                          viewModel.caricaSegnalazioni("CHIUSA")},
                colors = ButtonDefaults.buttonColors(containerColor = if (mostraArchivio) AccentBlue else Color.Gray),
                modifier = Modifier.weight(1f)
            ) { Text("Archivio", color = if (mostraArchivio) DarkNavy else WhiteText, fontWeight = FontWeight.Bold) }
        }

        if (segnalazioniFiltrate.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (mostraArchivio) "Nessuna segnalazione in archivio." else "Nessuna segnalazione da gestire.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(segnalazioniFiltrate) { segnalazione ->
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
                            viewModel.rifiutaSegnalazione(segnalazione.id.toLong(), adminId,
                                onSuccess = {
                                    Toast.makeText(context, "Segnalazione rifiutata.", Toast.LENGTH_SHORT).show()
                                    viewModel.caricaSegnalazioni()
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    )
                }
            }
        }
    }

    if (mostraModaleRisolvi) {
        AlertDialog(
            onDismissRequest = { mostraModaleRisolvi = false },
            title = { Text("Conferma Risoluzione", color = WhiteText) },
            text = { Text("Confermi la risoluzione della segnalazione (con eventuale rimozione dell'elemento)?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        segnalazioneSelezionata?.let { id ->
                            viewModel.risolviSegnalazione(id, adminId, sospendiAutore = false,
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

    if (mostraModaleBanna) {
        AlertDialog(
            onDismissRequest = { mostraModaleBanna = false },
            title = { Text("⚠ ATTENZIONE BAN", color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text("Stai per applicare la sanzione più dura sull'elemento e sospendere definitivamente l'utente. Confermi?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        segnalazioneSelezionata?.let { id ->
                            viewModel.risolviSegnalazione(id, adminId, sospendiAutore = true,
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
}

@Composable
fun CartaSegnalazione(
    segnalazione: SegnalazioneDTO,
    mostraAzioni: Boolean,
    onPrendiInCarico: () -> Unit,
    onRisolvi: () -> Unit,
    onBanna: () -> Unit,
    onRifiuta: () -> Unit
) {
    Surface(
        color = CardOverlay,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${segnalazione.id} - ${segnalazione.tipo}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Inviata da: ${segnalazione.segnalatoreUsername ?: "N/D"}", color = Color.LightGray, fontSize = 12.sp)
                    Text("Riferimento: ${segnalazione.riferimentoNome ?: "N/D"}", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Motivo: ${segnalazione.motivo}", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = segnalazione.descrizione ?: "Nessuna descrizione", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (mostraAzioni) {
                if (segnalazione.stato.toString() == "APERTA") {
                    Button(onClick = onPrendiInCarico, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), modifier = Modifier.fillMaxWidth()) {
                        Text("Prendi in carico", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                } else if (segnalazione.stato.toString() == "IN_LAVORAZIONE") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onRisolvi, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen), modifier = Modifier.weight(1f)) {
                            Text("✅ Risolvi", fontSize = 12.sp)
                        }
                        Button(onClick = onBanna, colors = ButtonDefaults.buttonColors(containerColor = DangerRed), modifier = Modifier.weight(1f)) {
                            Text("⛔ Banna", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRifiuta, colors = ButtonDefaults.buttonColors(containerColor = DangerRed), modifier = Modifier.fillMaxWidth()) {
                        Text("Rifiuta", color = WhiteText, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text("Segnalazione archiviata", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun SchermataUtentiBannati(viewModel: AdminViewModel) {
    val context = LocalContext.current
    var mostraModaleSbanna by remember { mutableStateOf(false) }
    var utenteDaSbannare by remember { mutableStateOf<Long?>(null) }

    val utentiBannati by viewModel.utentiBannati.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Utenti Bannati", color = WhiteText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (utentiBannati.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nessun utente bannato.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp), modifier = Modifier.fillMaxSize()) {
                items(utentiBannati) { utente ->
                    Surface(color = CardOverlay, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("👤 ${utente.username}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Data: ${utente.dataBan}", color = Color.LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Motivo: ${utente.motivoBan}", color = DangerRed, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    utenteDaSbannare = utente.id.toLong()
                                    mostraModaleSbanna = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) { Text("🔓 Sbanna", color = WhiteText, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }

    if (mostraModaleSbanna) {
        AlertDialog(
            onDismissRequest = { mostraModaleSbanna = false },
            title = { Text("Riattiva Utente", color = WhiteText, fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler riattivare questo utente? Potrà nuovamente accedere alla piattaforma.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        utenteDaSbannare?.let { id ->
                            viewModel.sbannaUtente(id,
                                onSuccess = {
                                    Toast.makeText(context, "Utente riattivato con successo!", Toast.LENGTH_SHORT).show()
                                    mostraModaleSbanna = false
                                    viewModel.caricaUtentiBannati()
                                },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleSbanna = false }) { Text("Annulla", color = Color.Gray) } },
            containerColor = DarkNavy
        )
    }
}

@Composable
fun CartaRichiestaEspandibile(
    richiesta: RichiestaPromozioneEntity,
    onApprova: () -> Unit,
    onRifiuta: () -> Unit,
    onScarica: () -> Unit,
    onVediMotivazione: () -> Unit,
    onVediBiografia: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = CardOverlay,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Candidato: ${richiesta.usernameViaggiatore}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Email: ${richiesta.emailProfessionale}", color = Color.LightGray, fontSize = 12.sp)
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Chiudi" else "Apri",
                    tint = Color.Gray,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val (statoColore, statoTesto) = when (richiesta.stato) {
                "IN_ATTESA" -> Color(0xFFF59E0B) to "IN ATTESA"
                "APPROVATA" -> SuccessGreen to "APPROVATA"
                else -> DangerRed to "RIFIUTATA"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statoColore.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(statoTesto, color = statoColore, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (isExpanded) {
                Column(modifier = Modifier.animateContentSize()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Data: ${richiesta.dataRichiesta.take(10)}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onVediMotivazione,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498db)),
                            modifier = Modifier.weight(1f)
                        ) { Text("Motivazione", fontSize = 12.sp) }
                        Button(
                            onClick = onVediBiografia,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498db)),
                            modifier = Modifier.weight(1f)
                        ) { Text("Biografia", fontSize = 12.sp) }
                    }

                    if (!richiesta.documentiLink.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onScarica,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("📄 Scarica Documento", color = DarkNavy, fontWeight = FontWeight.Bold) }
                    }

                    if (richiesta.stato == "IN_ATTESA") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onApprova,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.weight(1f)
                            ) { Text("✅ Approva", fontWeight = FontWeight.Bold) }
                            Button(
                                onClick = onRifiuta,
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                modifier = Modifier.weight(1f)
                            ) { Text("❌ Rifiuta", fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (richiesta.stato == "APPROVATA") "✅ Accettata" else "❌ Rifiutata",
                            color = if (richiesta.stato == "APPROVATA") SuccessGreen else DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun apriFile(context: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {

            val tipoCorretto = if (file.name.endsWith(".docx")) {
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            } else {
                "application/pdf"
            }

            setDataAndType(uri, tipoCorretto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Impossibile aprire il file", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScaffold(
    titolo: String,
    nomeUtente: String,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = DarkNavy, modifier = Modifier.width(300.dp)) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.AccountCircle, null, tint = WhiteText, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ciao, $nomeUtente", color = WhiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Person, null, tint = WhiteText) },
                    label = { Text("Il Mio Profilo", color = WhiteText) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, ProfiloActivity::class.java))
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = DangerRed) },
                    label = { Text("Disconnetti", color = DangerRed, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val sessionManager = SessionManager(context)
                        sessionManager.cancellaSessione()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    titolo = titolo, nomeUtente = nomeUtente, mostraFrecciaIndietro = false,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }
}