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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.*
import com.example.enterprisemobile.ui.components.TopBar
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
    val focusManager = LocalFocusManager.current

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
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(modifier = Modifier.padding(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Download documento...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialog per richieste (mantenuti qui perché specifici delle richieste)
    if (mostraModaleMotivazione) {
        AlertDialog(
            onDismissRequest = { mostraModaleMotivazione = false },
            title = { Text("Motivazione della Candidatura", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(testoDaVisualizzare, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { mostraModaleMotivazione = false }) { Text("Chiudi", color = MaterialTheme.colorScheme.primary) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (mostraModaleBiografia) {
        AlertDialog(
            onDismissRequest = { mostraModaleBiografia = false },
            title = { Text("Biografia Professionale", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(testoDaVisualizzare, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { mostraModaleBiografia = false }) { Text("Chiudi", color = MaterialTheme.colorScheme.primary) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (mostraModaleRifiuto) {
        AlertDialog(
            onDismissRequest = { mostraModaleRifiuto = false },
            title = { Text("Rifiuta Candidatura", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text("Inserisci il motivo del rifiuto:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivazioneRifiuto,
                        onValueChange = { motivazioneRifiuto = it },
                        modifier = Modifier.fillMaxWidth(),
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
                                onError = { err: String -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Conferma Rifiuto") }
            },
            dismissButton = {
                TextButton(onClick = { mostraModaleRifiuto = false }) { Text("Annulla", color = MaterialTheme.colorScheme.outline) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (mostraModaleConfermaApprova) {
        AlertDialog(
            onDismissRequest = { mostraModaleConfermaApprova = false },
            title = { Text("Conferma Approvazione", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "Sei sicuro di voler approvare questa candidatura? L'utente verrà promosso a Organizzatore e riceverà le credenziali di accesso via email.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                onError = { err: String ->
                                    Toast.makeText(context, "Errore: $err", Toast.LENGTH_LONG).show()
                                    mostraModaleConfermaApprova = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("✅ Conferma") }
            },
            dismissButton = {
                TextButton(onClick = { mostraModaleConfermaApprova = false }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    AdminScaffold(
        titolo = "MOVEON",
        nomeUtente = nomeAdmin,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Richieste") },
                    label = { Text("Richieste", fontSize = 12.sp) },
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Warning, contentDescription = "Segnalazioni") },
                    label = { Text("Segnalazioni", fontSize = 12.sp) },
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Block, contentDescription = "Ban") },
                    label = { Text("Ban", fontSize = 12.sp) },
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 1f)).padding(innerPadding)) {
            if (isLoading && richieste.isEmpty() && selectedBottomTab == 0) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else {
                when (selectedBottomTab) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Richieste di Promozione", color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { vistaAttuale = "PENDENTI"; viewModel.filtraPerStato("IN_ATTESA") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vistaAttuale == "PENDENTI") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (vistaAttuale == "PENDENTI") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Richieste Pendenti", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                Button(
                                    onClick = { vistaAttuale = "STORICO"; viewModel.filtraPerStato(null) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vistaAttuale == "STORICO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (vistaAttuale == "STORICO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Storico Valutazioni", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = queryRicerca,
                                onValueChange = { queryRicerca = it },
                                label = { Text("Cerca per username...") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                                        if (queryRicerca.isNotEmpty()) {
                                            IconButton(onClick = {
                                                queryRicerca = ""
                                                viewModel.cercaPerUsername("")
                                            }) {
                                                Icon(Icons.Filled.Clear, "Cancella")
                                            }
                                        }
                                        IconButton(onClick = {
                                            focusManager.clearFocus()
                                            viewModel.cercaPerUsername(queryRicerca)
                                        }) {
                                            Icon(Icons.Filled.Search, "Cerca", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        viewModel.cercaPerUsername(queryRicerca)
                                    }
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
                                    Text("Nessuna richiesta trovata in questa pagina.", color = MaterialTheme.colorScheme.outline)
                                }
                            } else if (richiesteFiltrate.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Nessuna richiesta trovata.", color = MaterialTheme.colorScheme.outline)
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
                                                    containerColor = if (paginaCorrente > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    contentColor = if (paginaCorrente > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            ) { Text("Prec", fontSize = 14.sp) }

                                            Text(
                                                text = "Pagina ${paginaCorrente + 1} di ${totalePagine.coerceAtLeast(1)}",
                                                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium
                                            )

                                            Button(
                                                onClick = { viewModel.paginaSuccessiva() },
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
                    1 -> GestioneSegnalazioniScreen(viewModel)
                    2 -> GestioneBanScreen(viewModel)
                }
            }
        }
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
        color = MaterialTheme.colorScheme.surfaceVariant,
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
                    Text("Candidato: ${richiesta.usernameViaggiatore}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Email: ${richiesta.emailProfessionale}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Chiudi" else "Apri",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val (statoColore, statoTesto) = when (richiesta.stato) {
                "IN_ATTESA" -> MaterialTheme.colorScheme.tertiary to "IN ATTESA"
                "APPROVATA" -> MaterialTheme.colorScheme.primary to "APPROVATA"
                else -> MaterialTheme.colorScheme.error to "RIFIUTATA"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statoColore.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(statoTesto, color = statoColore, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (isExpanded) {
                Column(modifier = Modifier.animateContentSize()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Data: ${richiesta.dataRichiesta.take(10)}", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onVediMotivazione,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) { Text("Motivazione", fontSize = 12.sp) }
                        Button(
                            onClick = onVediBiografia,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) { Text("Biografia", fontSize = 12.sp) }
                    }

                    if (!richiesta.documentiLink.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onScarica,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("📄 Scarica Documento", fontWeight = FontWeight.Bold) }
                    }

                    if (richiesta.stato == "IN_ATTESA") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onApprova,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) { Text("✅ Approva", fontWeight = FontWeight.Bold) }
                            Button(
                                onClick = onRifiuta,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) { Text("❌ Rifiuta", fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (richiesta.stato == "APPROVATA") "✅ Accettata" else "❌ Rifiutata",
                            color = if (richiesta.stato == "APPROVATA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
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
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.width(300.dp)) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.AccountCircle, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ciao, $nomeUtente", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onSurface) },
                    label = { Text("Il Mio Profilo", color = MaterialTheme.colorScheme.onSurface) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, ProfiloActivity::class.java))
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Disconnetti", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
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