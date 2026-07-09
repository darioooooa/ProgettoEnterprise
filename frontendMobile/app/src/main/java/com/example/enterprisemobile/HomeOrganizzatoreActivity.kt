package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.data.repository.AdminRepository
import com.example.enterprisemobile.model.ViaggioMappaDTO
import com.example.enterprisemobile.ui.StatisticheOrganizzatoreScreen
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.ui.components.SchermataDellaChat
import com.example.enterprisemobile.viewmodels.ChatViewModel
import com.example.enterprisemobile.viewmodels.GeneratoreChatViewModel
import com.example.enterprisemobile.viewmodels.HomeOrganizzatoreViewModel
import com.example.enterprisemobile.viewmodels.StatisticheOrganizzatoreViewModel
import com.example.enterprisemobile.viewmodels.ViaggioViewModel
import com.example.enterprisemobile.viewmodels.ViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import kotlinx.coroutines.launch

class HomeOrganizzatoreActivity : ComponentActivity() {
    private val viaggioViewModel: ViaggioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val usernameRicevuto = intent.getStringExtra("CHIAVE_USERNAME") ?: "Organizzatore"

        MapboxOptions.accessToken = com.example.enterprisemobile.BuildConfig.MAPBOX_TOKEN

        setContent {
            EnterpriseMobileTheme {
                SchermataOrganizzatore(usernameRicevuto, viaggioViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, MapboxExperimental::class)
@Composable
fun MappaItinerari(
    viaggi: List<ViaggioMappaDTO>,
    mapViewportState: MapViewportState,
    markerIcon: android.graphics.Bitmap?,
    onMarkerClick: (List<ViaggioMappaDTO>) -> Unit
) {
    android.util.Log.d("MAPBOX_PERF", "Rendering del componente MapboxMap")

    val viaggiRaggruppati = remember(viaggi) {
        viaggi.filter { v -> v.latitudine != null && v.longitudine != null }
            .groupBy { v ->
                val latChiave = String.format("%.4f", v.latitudine)
                val lngChiave = String.format("%.4f", v.longitudine)
                "$latChiave|$lngChiave"
            }
    }

    MapboxMap(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
        mapViewportState = mapViewportState
    ) {
        android.util.Log.d("MAPBOX_PERF", "Disegno di ${viaggiRaggruppati.size} cluster di marker stabili")

        viaggiRaggruppati.forEach { (coordinataChiave, listaViaggiInPunto) ->
            val primoViaggio = listaViaggiInPunto.first()

            key(coordinataChiave) {
                PointAnnotation(
                    point = Point.fromLngLat(primoViaggio.longitudine, primoViaggio.latitudine),
                    iconImageBitmap = markerIcon,
                    onClick = {
                        onMarkerClick(listaViaggiInPunto)
                        true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, MapboxExperimental::class)
@Composable
fun SchermataOrganizzatore(nomeUtente: String, viaggioViewModel: ViaggioViewModel) {
    val context = LocalContext.current
    val sessionManager = remember { com.example.enterprisemobile.data.security.SessionManager(context) }
    val apiService = remember(context) { RetrofitClient.ottieniViaggioService(context) }
    val utenteApiService = remember(context) { RetrofitClient.ottieniUtenteService(context) }
    val prenotazioniApiService = remember(context) { RetrofitClient.ottieniPrenotazioneService(context) }

    val database = remember(context) { AppDatabase.getInstance(context) }
    val repository = remember(context) { ViaggioRepository(apiService, database.viaggioDao()) }
    val prenotazioneRepository = remember(context) { PrenotazioneRepository(prenotazioniApiService, database.prenotazioneDao()) }
    val adminRepository = remember(context) { AdminRepository(context) }

    // ViewModels
    val viewModel: HomeOrganizzatoreViewModel = viewModel(
        factory = ViewModelFactory(repository, prenotazioneRepository, adminRepository)
    )
    val viewModelStatistiche: StatisticheOrganizzatoreViewModel = viewModel()
    val modelloDiVistaChat: ChatViewModel = viewModel(
        factory = GeneratoreChatViewModel(context)
    )

    // Stati Chat
    val listaDelleStanzeReali by modelloDiVistaChat.stanzeVisibili.collectAsState()
    var identificativoStanzaSelezionata by rememberSaveable { mutableStateOf<Long?>(null) }
    val totaleNotifiche = listaDelleStanzeReali.sumOf { it.numeroMessaggiNonLetti }

    // Stati Mappa
    val viaggi by viewModel.viaggi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Stati Prenotazioni
    val prenotazioni by viewModel.prenotazioni.collectAsState()
    val paginaCorrente by viewModel.paginaCorrente.collectAsState()
    val totalePagine by viewModel.totalePagine.collectAsState()
    val isLoadingPrenotazioni by viewModel.isLoadingPrenotazioni.collectAsState()
    val filtroStato by viewModel.filtroStato.collectAsState()

    // Stati per dialog segnalazione
    val showSegnalazioneDialog by viewModel.showSegnalazioneDialog.collectAsState()
    val viaggiatoreDaSegnalare by viewModel.viaggiatoreDaSegnalare.collectAsState()
    val isLoadingSegnalazione by viewModel.isLoadingSegnalazione.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val listaViaggiCercati by viaggioViewModel.viaggiSalvati.collectAsState()

    var sottoVistaMappa by remember { mutableStateOf("MAPPA") }

    // Stati di Navigazione
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var vistaDashboard by rememberSaveable { mutableStateOf("MAPPA") }

    // Caricamento asincrono iniziale
    LaunchedEffect(Unit) {
        viewModel.caricaDatiMappa()
        modelloDiVistaChat.caricaLeMieStanzeOrganizzatore(nomeUtente)
        modelloDiVistaChat.attivaAscoltoNotificheOrganizzatore(nomeUtente)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokenRicevuto = task.result
                launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        utenteApiService.aggiornaToken(mapOf("token" to tokenRicevuto))
                    } catch (e: Exception) {
                        android.util.Log.e("FCM_API_ERROR", "Errore invio token", e)
                    }
                }
            }
        }
    }
    //serve per refreshare la pagina e mostrare la sezione messaggi aggiornata
    //senza dover rricaricare il progetto
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                modelloDiVistaChat.caricaLeMieStanzeOrganizzatore(nomeUtente)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = selectedItem != 0 || identificativoStanzaSelezionata != null || vistaDashboard == "PRENOTAZIONI") {
        if (selectedItem == 2 && identificativoStanzaSelezionata != null) {
            // Se l'organizzatore ha una chat aperta, torna alla lista delle conversazioni
            modelloDiVistaChat.esciDallaStanza()
            identificativoStanzaSelezionata = null
        } else if (selectedItem == 0 && vistaDashboard == "PRENOTAZIONI") {
            // Se l'organizzatore è nella home ma guarda la sottoscheda prenotazioni, torna alla mappa
            vistaDashboard = "MAPPA"
        } else {
            // In tutti gli altri casi (statistiche o lista messaggi), ritorna alla Home/Mappa
            selectedItem = 0
            vistaDashboard = "MAPPA"
        }
    }

    LaunchedEffect(vistaDashboard) {
        if (vistaDashboard == "PRENOTAZIONI" && prenotazioni.isEmpty()) {
            viewModel.caricaPrenotazioniOrganizzatore(0)
        }
    }

    val items = listOf("Home", "Statistiche", "Messaggi")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.BarChart, Icons.Filled.Email)

    AdminScaffold(
        titolo = "MOVEON",
        nomeUtente = nomeUtente,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            if (index == 2 && totaleNotifiche > 0) {
                                BadgedBox(
                                    badge = { Badge(containerColor = MaterialTheme.colorScheme.error) { Text(totaleNotifiche.toString(), color = MaterialTheme.colorScheme.onError) } }
                                ) { Icon(icons[index], contentDescription = item) }
                            } else {
                                Icon(icons[index], contentDescription = item)
                            }
                        },
                        label = { Text(item, fontSize = 12.sp) },
                        selected = selectedItem == index,
                        onClick = {
                            if (selectedItem != index) {
                                keyboardController?.hide()
                                modelloDiVistaChat.esciDallaStanza()
                                identificativoStanzaSelezionata = null
                                selectedItem = index
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 1f))
            .padding(paddingValues)
        ) {
            when (selectedItem) {
                // Tab dashboard
                0 -> {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { vistaDashboard = "MAPPA" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (vistaDashboard == "MAPPA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (vistaDashboard == "MAPPA") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(50)
                            ) { Text("Mappa Itinerari", fontSize = 14.sp, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = { vistaDashboard = "PRENOTAZIONI" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (vistaDashboard == "PRENOTAZIONI") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (vistaDashboard == "PRENOTAZIONI") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(50)
                            ) { Text("Prenotazioni", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            val isMappaVisibile = vistaDashboard == "MAPPA"

                            // Strato mappa / ricerca
                            Column(modifier = Modifier.fillMaxSize().alpha(if (isMappaVisibile) 1f else 0f)) {

                                BarraSottoMappaUnita(
                                    vistaAttuale = sottoVistaMappa,
                                    onVistaCambio = { nuovaVista -> sottoVistaMappa = nuovaVista }
                                )

                                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {

                                    val isSottoMappaAttiva = sottoVistaMappa == "MAPPA"

                                    Column(modifier = Modifier.fillMaxSize().alpha(if (isSottoMappaAttiva) 1f else 0f)) {
                                        val mapViewportState = rememberMapViewportState()
                                        LaunchedEffect(Unit) {
                                            mapViewportState.setCameraOptions {
                                                center(Point.fromLngLat(12.4964, 41.9028))
                                                zoom(5.0)
                                            }
                                        }
                                        val markerIcon = remember(context) { ContextCompat.getDrawable(context, R.drawable.viaggio_marker)?.toBitmap() }
                                        var viaggiSelezionatiInMarker by remember { mutableStateOf<List<ViaggioMappaDTO>>(emptyList()) }

                                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                            MappaItinerari(
                                                viaggi = viaggi,
                                                mapViewportState = mapViewportState,
                                                markerIcon = markerIcon,
                                                onMarkerClick = { listaViaggi -> if (isSottoMappaAttiva && isMappaVisibile) viaggiSelezionatiInMarker = listaViaggi }
                                            )

                                            if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)

                                            if (viaggiSelezionatiInMarker.isNotEmpty()) {
                                                Card(
                                                    modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp).fillMaxWidth().wrapContentHeight(),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = if (viaggiSelezionatiInMarker.size > 1) "🗺️ ${viaggiSelezionatiInMarker.size} viaggi qui" else "📍 Viaggio in questa posizione",
                                                                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary
                                                            )
                                                            IconButton(onClick = { viaggiSelezionatiInMarker = emptyList() }, modifier = Modifier.size(24.dp)) {
                                                                Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = MaterialTheme.colorScheme.outline)
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Box(modifier = Modifier.heightIn(max = 180.dp)) {
                                                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                items(viaggiSelezionatiInMarker.size) { index ->
                                                                    val viaggio = viaggiSelezionatiInMarker[index]
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Text(viaggio.titolo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                                                        Button(
                                                                            onClick = {
                                                                                val intent = Intent(context, DettaglioViaggioActivity::class.java)
                                                                                intent.putExtra("VIAGGIO_ID", viaggio.id)
                                                                                context.startActivity(intent)
                                                                                viaggiSelezionatiInMarker = emptyList()
                                                                            },
                                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                                            modifier = Modifier.height(32.dp)
                                                                        ) { Text("Vedi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { if (isSottoMappaAttiva && isMappaVisibile) context.startActivity(Intent(context, CreaViaggioActivity::class.java)) },
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) { Text("+ Crea Nuovo Viaggio", fontWeight = FontWeight.Bold) }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    if (!isSottoMappaAttiva) Spacer(modifier = Modifier.fillMaxSize().clickable(enabled = false) {})

                                    if (sottoVistaMappa == "CERCA") {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {

                                            item {
                                                BoxRicerca(
                                                    onCercaClick = { dest, dMin, dMax, posti, pMin, pMax ->
                                                        viaggioViewModel.cercaViaggi(dest, dMin, dMax, posti, pMin, pMax, 0)
                                                    }
                                                )
                                            }

                                            if (viaggioViewModel.ricercaEffettuata) {
                                                if (listaViaggiCercati.isEmpty()) {
                                                    item {
                                                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                                            Text("Nessun viaggio trovato.", color = MaterialTheme.colorScheme.outline, fontSize = 16.sp)
                                                        }
                                                    }
                                                } else {
                                                    items(listaViaggiCercati, key = { it.id }) { viaggio ->
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.fillMaxWidth().clickable {
                                                                val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                                                                    putExtra("VIAGGIO_ID", viaggio.id)
                                                                }
                                                                context.startActivity(intent)
                                                            }
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(viaggio.titolo, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Text("🌍 ${viaggio.destinazione}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 14.sp)
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Text("💰 ${viaggio.prezzo} €", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                                }

                                                                Button(
                                                                    onClick = {
                                                                        val intent = Intent(context, DettaglioViaggioActivity::class.java)
                                                                        intent.putExtra("VIAGGIO_ID", viaggio.id)
                                                                        context.startActivity(intent)
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                                    modifier = Modifier.height(36.dp)
                                                                ) {
                                                                    Text("Vedi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
                                                                }
                                                            }
                                                        }
                                                    }

                                                    item {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    viaggioViewModel.cercaViaggi("", "", "", "", "0", "5000", viaggioViewModel.paginaCorrente - 1)
                                                                },
                                                                enabled = viaggioViewModel.paginaCorrente > 0,
                                                                colors = ButtonDefaults.buttonColors(containerColor = if (viaggioViewModel.paginaCorrente > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                            ) { Text("Prec", fontSize = 14.sp) }

                                                            Text(text = "Pagina ${viaggioViewModel.paginaCorrente + 1} di ${viaggioViewModel.totalePagine.coerceAtLeast(1)}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)

                                                            Button(
                                                                onClick = {
                                                                    viaggioViewModel.cercaViaggi("", "", "", "", "0", "5000", viaggioViewModel.paginaCorrente + 1)
                                                                },
                                                                enabled = viaggioViewModel.paginaCorrente < viaggioViewModel.totalePagine - 1,
                                                                colors = ButtonDefaults.buttonColors(containerColor = if (viaggioViewModel.paginaCorrente < viaggioViewModel.totalePagine - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                            ) { Text("Succ", fontSize = 14.sp) }
                                                        }
                                                        Spacer(modifier = Modifier.height(80.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (!isMappaVisibile) Spacer(modifier = Modifier.fillMaxSize().clickable(enabled = false) {})

                            // Prenotazioni
                            if (vistaDashboard == "PRENOTAZIONI") {
                                Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

                                    BarraFiltriUnita(
                                        filtroAttuale = filtroStato,
                                        onFiltroCambiato = { nuovoStato -> viewModel.impostaFiltroStato(nuovoStato) }
                                    )

                                    // Barra di ricerca username
                                    var queryRicerca by rememberSaveable { mutableStateOf("") }

                                    OutlinedTextField(
                                        value = queryRicerca,
                                        onValueChange = { queryRicerca = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .height(56.dp),
                                        placeholder = { Text("Cerca per username...", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                viewModel.impostaFiltroUsername(queryRicerca)
                                            }
                                        ),
                                        leadingIcon = {
                                            IconButton(onClick = {
                                                viewModel.impostaFiltroUsername(queryRicerca)
                                            }) {
                                                Icon(Icons.Default.Search, "Cerca", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        },
                                        trailingIcon = {
                                            if (queryRicerca.isNotEmpty()) {
                                                IconButton(onClick = {
                                                    queryRicerca = ""
                                                    viewModel.impostaFiltroUsername("")
                                                }) {
                                                    Icon(Icons.Default.Clear, "Cancella", tint = MaterialTheme.colorScheme.outline)
                                                }
                                            }
                                        }
                                    )

                                    if (isLoadingPrenotazioni) {
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else if (prenotazioni.isEmpty()) {
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Text("Nessuna prenotazione trovato.", color = MaterialTheme.colorScheme.outline)
                                        }
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            items(prenotazioni, key = { it.id }) { prenotazione ->
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Utente: ${prenotazione.viaggiatoreUsername ?: "Sconosciuto"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                            Text("Viaggio: ${prenotazione.viaggioTitolo}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)

                                                            Spacer(modifier = Modifier.height(8.dp))

                                                            val (coloreBadge, testoBadge) = when (prenotazione.stato) {
                                                                "CONFERMATA" -> MaterialTheme.colorScheme.primary to "CONFERMATA"
                                                                "ANNULLATA" -> MaterialTheme.colorScheme.error to "ANNULLATA"
                                                                else -> MaterialTheme.colorScheme.outline to prenotazione.stato
                                                            }

                                                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(coloreBadge.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                                                Text(testoBadge, color = coloreBadge, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        // Pulsante Segnala
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.apriDialogSegnalazione(
                                                                    viaggiatoreId = prenotazione.viaggiatoreId ?: 0L,
                                                                    viaggiatoreUsername = prenotazione.viaggiatoreUsername ?: "Sconosciuto",
                                                                    viaggioTitolo = prenotazione.viaggioTitolo ?: ""
                                                                )
                                                            }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Flag,
                                                                contentDescription = "Segnala",
                                                                tint = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Paginazione
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { viewModel.caricaPrenotazioniOrganizzatore(paginaCorrente - 1) },
                                            enabled = paginaCorrente > 0,
                                            colors = ButtonDefaults.buttonColors(containerColor = if (paginaCorrente > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        ) { Text("Prec", fontSize = 14.sp) }

                                        Text(text = "Pagina ${paginaCorrente + 1} di ${totalePagine.coerceAtLeast(1)}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)

                                        Button(
                                            onClick = { viewModel.caricaPrenotazioniOrganizzatore(paginaCorrente + 1) },
                                            enabled = paginaCorrente < totalePagine - 1,
                                            colors = ButtonDefaults.buttonColors(containerColor = if (paginaCorrente < totalePagine - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        ) { Text("Succ", fontSize = 14.sp) }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sezione statistiche
                1 -> {
                    StatisticheOrganizzatoreScreen(viewModel = viewModelStatistiche)
                }

                // Sezione messaggi
                2 -> {
                    if (identificativoStanzaSelezionata == null) {
                        if (listaDelleStanzeReali.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nessuna conversazione attiva al momento.", color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listaDelleStanzeReali) { stanzaCorrente ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                identificativoStanzaSelezionata = stanzaCorrente.identificativoStanza
                                                modelloDiVistaChat.azzeraNotificheStanzaOrganizzatore(stanzaCorrente.identificativoStanza, nomeUtente)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(stanzaCorrente.titoloDelViaggio, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Viaggiatore: ${stanzaCorrente.nomeUtenteViaggiatore}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                            }

                                            if (stanzaCorrente.numeroMessaggiNonLetti > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(start = 8.dp).size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(stanzaCorrente.numeroMessaggiNonLetti.toString(), color = MaterialTheme.colorScheme.onError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextButton(
                                onClick = {
                                    modelloDiVistaChat.esciDallaStanza()
                                    identificativoStanzaSelezionata = null
                                },
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            ) { Text(text = "⬅ Torna alla lista delle chat", color = MaterialTheme.colorScheme.primary) }

                            SchermataDellaChat(
                                modelloDiVistaChat = modelloDiVistaChat,
                                identificativoDellaStanza = identificativoStanzaSelezionata!!,
                                nomeDelMittenteLocale = nomeUtente,
                                seiOrganizzatore = true,
                                identificativoUtenteLocale = sessionManager.ottieniIdUtente()?.toLongOrNull() ?: 0L,
                                onIndietroPremuto = {
                                    identificativoStanzaSelezionata = null
                                }
                            )
                        }
                    }
                }
            }

            // Dialog segnalazione viaggiatore
            if (showSegnalazioneDialog && viaggiatoreDaSegnalare != null) {
                DialogSegnalazioneViaggiatore(
                    username = viaggiatoreDaSegnalare!!.second,
                    isLoading = isLoadingSegnalazione,
                    onDismiss = { viewModel.chiudiDialogSegnalazione() },
                    onInvia = { motivo, descrizione ->
                        val orgId = sessionManager.ottieniIdUtente()?.toLongOrNull() ?: 0L
                        viewModel.inviaSegnalazione(
                            organizzatoreId = orgId,
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogSegnalazioneViaggiatore(
    username: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onInvia: (String, String) -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    var descrizione by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { if (!isLoading) onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clickable(enabled = false) { }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🚩 Segnala viaggiatore",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = { if (!isLoading) onDismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "di $username",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Aiutaci a mantenere la piattaforma sicura. Seleziona il motivo per cui stai segnalando questo utente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(16.dp))

                // Label motivo
                Text(
                    "Motivo della segnalazione *",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = when (motivo) {
                            "SPAM" -> "Spam o Truffa"
                            "COMPORTAMENTO_SCORRETTO" -> "Comportamento Scorretto / Non Pagante"
                            "FALSO" -> "Contenuto Falso / Inappropriato"
                            "ALTRO" -> "Altro"
                            else -> "-- Seleziona un motivo --"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        listOf(
                            "SPAM" to "Spam o Truffa",
                            "COMPORTAMENTO_SCORRETTO" to "Comportamento Scorretto / Non Pagante",
                            "FALSO" to "Contenuto Falso / Inappropriato",
                            "ALTRO" to "Altro"
                        ).forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { motivo = value; expanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Label dettagli
                Text(
                    "Dettagli aggiuntivi (opzionale)",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = descrizione,
                    onValueChange = { descrizione = it },
                    placeholder = { Text("Scrivi qui i dettagli per aiutare gli amministratori...", color = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.height(20.dp))

                // Bottoni
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
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onInvia(motivo, descrizione) },
                        enabled = motivo.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (motivo.isNotBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (motivo.isNotBlank()) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Invia Segnalazione")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarraSottoMappaUnita(
    vistaAttuale: String,
    onVistaCambio: (String) -> Unit
) {
    val opzioni = listOf("MAPPA" to "La tua Mappa", "CERCA" to "Cerca Viaggi")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        opzioni.forEach { (valore, etichetta) ->
            val isSelected = vistaAttuale == valore
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onVistaCambio(valore) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etichetta,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BarraFiltriUnita(
    filtroAttuale: String?,
    onFiltroCambiato: (String?) -> Unit
) {
    val opzioni = listOf(
        null to "Tutte",
        "CONFERMATA" to "Confermate",
        "ANNULLATA" to "Annullate"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        opzioni.forEach { (valore, etichetta) ->
            val isSelected = filtroAttuale == valore
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onFiltroCambiato(valore) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etichetta,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BoxRicerca(
    onCercaClick: (destinazione: String, dataMin: String, dataMax: String, posti: String, prezzoMin: String, prezzoMax: String) -> Unit
) {
    var destinazione by rememberSaveable { mutableStateOf("") }
    var dataMin by rememberSaveable { mutableStateOf("") }
    var dataMax by rememberSaveable { mutableStateOf("") }
    var posti by rememberSaveable { mutableStateOf("") }
    var prezzoMin by rememberSaveable { mutableStateOf("") }
    var prezzoMax by rememberSaveable { mutableStateOf("") }
    var mostraAvanzati by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            SearchInput("Destinazione", destinazione) { destinazione = it }
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchInput("Dal", dataMin, Modifier.weight(1f)) { dataMin = it }
                SearchInput("Al", dataMax, Modifier.weight(1f)) { dataMax = it }
            }
            Spacer(modifier = Modifier.height(12.dp))

            SearchInput("Posti minimi", posti) { posti = it }

            TextButton(onClick = { mostraAvanzati = !mostraAvanzati }) {
                Text(
                    text = if (mostraAvanzati) "🔼 Nascondi filtri prezzo" else "🔽 Mostra filtri prezzo",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (mostraAvanzati) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchInput("Min €", prezzoMin, Modifier.weight(1f)) { prezzoMin = it }
                    SearchInput("Max €", prezzoMax, Modifier.weight(1f)) { prezzoMax = it }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onCercaClick(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cerca Viaggi", fontWeight = FontWeight.Bold)
            }
        }
    }
}