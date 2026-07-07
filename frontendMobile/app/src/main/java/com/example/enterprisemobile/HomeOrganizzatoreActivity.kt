package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.PrenotazioneRepository
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.ViaggioMappaDTO
import com.example.enterprisemobile.ui.StatisticheOrganizzatoreScreen
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.ui.components.SchermataDellaChat
import com.example.enterprisemobile.viewmodels.ChatViewModel
import com.example.enterprisemobile.viewmodels.GeneratoreChatViewModel
import com.example.enterprisemobile.viewmodels.HomeOrganizzatoreViewModel
import com.example.enterprisemobile.viewmodels.StatisticheOrganizzatoreViewModel
import com.example.enterprisemobile.viewmodels.ViewModelFactory
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import kotlinx.coroutines.launch

class HomeOrganizzatoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val usernameRicevuto = intent.getStringExtra("CHIAVE_USERNAME") ?: "Organizzatore"

        MapboxOptions.accessToken = com.example.enterprisemobile.BuildConfig.MAPBOX_TOKEN

        setContent {
            EnterpriseMobileTheme {
                SchermataOrganizzatore(usernameRicevuto)
            }
        }
    }
}

// LOGICA DEI COMPAGNI MANTENUTA INTATTA: Clustering e stabilità marker Mapbox
@OptIn(MapboxExperimental::class)
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
fun SchermataOrganizzatore(nomeUtente: String) {


    val context = LocalContext.current
    val apiService = remember(context) { RetrofitClient.ottieniViaggioService(context) }
    val utenteApiService = remember(context) { RetrofitClient.ottieniUtenteService(context) }
    val prenotazioniApiService = remember(context) { RetrofitClient.ottieniPrenotazioneService(context) }

    val database = remember(context) { AppDatabase.getInstance(context) }
    val repository = remember(context) { ViaggioRepository(apiService, database.viaggioDao()) }
    val prenotazioneRepository = remember(context) { PrenotazioneRepository(prenotazioniApiService, database.prenotazioneDao()) }

    // ViewModels
    val viewModel: HomeOrganizzatoreViewModel = viewModel(
        factory = ViewModelFactory(repository, prenotazioneRepository)
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

    // Stati di Navigazione
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var vistaDashboard by remember { mutableStateOf("MAPPA") }

    // Caricamento asincrono iniziale
    LaunchedEffect(Unit) {
        viewModel.caricaDatiMappa()
        modelloDiVistaChat.caricaLeMieStanzeOrganizzatore(nomeUtente)
        modelloDiVistaChat.attivaAscoltoNotificheOrganizzatore(nomeUtente)

        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
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

    LaunchedEffect(vistaDashboard) {
        if (vistaDashboard == "PRENOTAZIONI" && prenotazioni.isEmpty()) {
            viewModel.caricaPrenotazioniOrganizzatore(0)
        }
    }

    val items = listOf("Home", "Statistiche", "Messaggi")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.BarChart, Icons.Filled.Email)

    // utilizzato admin scaffold per mantenere la stessa base grafica
    AdminScaffold(
        titolo = "DASHBOARD",
        nomeUtente = nomeUtente,
        bottomBar = {
            NavigationBar(containerColor = DarkNavy) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            if (index == 2 && totaleNotifiche > 0) {
                                BadgedBox(
                                    badge = { Badge(containerColor = DangerRed) { Text(totaleNotifiche.toString(), color = WhiteText) } }
                                ) { Icon(icons[index], contentDescription = item) }
                            } else {
                                Icon(icons[index], contentDescription = item)
                            }
                        },
                        label = { Text(item, fontSize = 12.sp) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WhiteText,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = CardOverlay
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> {
                    // TAB DASHBOARD (MAPPA / PRENOTAZIONI CON Z-INDEX)
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Bottoni per switchare
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { vistaDashboard = "MAPPA" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (vistaDashboard == "MAPPA") AccentBlue else Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("Mappa Itinerari", fontSize = 12.sp, color = DarkNavy, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = { vistaDashboard = "PRENOTAZIONI" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (vistaDashboard == "PRENOTAZIONI") AccentBlue else Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("Prenotazioni", fontSize = 12.sp, color = if (vistaDashboard == "PRENOTAZIONI") DarkNavy else WhiteText, fontWeight = FontWeight.Bold) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        //per evitare che la mappa venga ricaricata ogni volta che si andava nella sua schermata
                        //ora semplicemente va in background e diventa invisibile per permettere di mostrarele prenotazioni
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            val isMappaVisibile = vistaDashboard == "MAPPA"
                            Column(modifier = Modifier.fillMaxSize().alpha(if (isMappaVisibile) 1f else 0f)) {
                                val mapViewportState = rememberMapViewportState()
                                LaunchedEffect(Unit) {
                                    mapViewportState.setCameraOptions {
                                        center(Point.fromLngLat(12.4964, 41.9028))
                                        zoom(5.0)
                                    }
                                }
                                //marker per mostrare i viaggi sulla mappa
                                val markerIcon = remember(context) { ContextCompat.getDrawable(context, R.drawable.viaggio_marker)?.toBitmap() }
                                var viaggiSelezionatiInMarker by remember { mutableStateOf<List<ViaggioMappaDTO>>(emptyList()) }

                                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    MappaItinerari(
                                        viaggi = viaggi,
                                        mapViewportState = mapViewportState,
                                        markerIcon = markerIcon,
                                        onMarkerClick = { listaViaggi -> if (isMappaVisibile) viaggiSelezionatiInMarker = listaViaggi }
                                    )

                                    if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentBlue)

                                    // Popup Cluster in Stile Scuro
                                    if (viaggiSelezionatiInMarker.isNotEmpty()) {
                                        Card(
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp).fillMaxWidth().wrapContentHeight(),
                                            colors = CardDefaults.cardColors(containerColor = CardOverlay),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        //se ci sono piu viaggi in un stesso punto
                                                        text = if (viaggiSelezionatiInMarker.size > 1) "🗺️ ${viaggiSelezionatiInMarker.size} viaggi qui" else "📍 Viaggio in questa posizione",
                                                        fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentBlue
                                                    )
                                                    IconButton(onClick = { viaggiSelezionatiInMarker = emptyList() }, modifier = Modifier.size(24.dp)) {
                                                        Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = Color.Gray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(modifier = Modifier.heightIn(max = 180.dp)) {
                                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        items(viaggiSelezionatiInMarker.size) { index ->
                                                            val viaggio = viaggiSelezionatiInMarker[index]
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth().background(DarkNavy, shape = RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(viaggio.titolo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WhiteText, modifier = Modifier.weight(1f))
                                                                Button(
                                                                    onClick = {
                                                                        val intent = Intent(context, DettaglioViaggioActivity::class.java)
                                                                        intent.putExtra("VIAGGIO_ID", viaggio.id)
                                                                        context.startActivity(intent)
                                                                        viaggiSelezionatiInMarker = emptyList()
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                                    modifier = Modifier.height(32.dp)
                                                                ) { Text("Vedi", fontSize = 12.sp, color = DarkNavy, fontWeight = FontWeight.Bold) }
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
                                    onClick = { if (isMappaVisibile) context.startActivity(Intent(context, CreaViaggioActivity::class.java)) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) { Text("+ Crea Nuovo Viaggio", color = WhiteText, fontWeight = FontWeight.Bold) }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Blocco touch
                            if (!isMappaVisibile) Spacer(modifier = Modifier.fillMaxSize().clickable(enabled = false) {})

                            // strato 2: prenotazione che va a piazzarsi sopra la mappa per non doverla ricaricare ogni volta
                            if (vistaDashboard == "PRENOTAZIONI") {
                                Column(modifier = Modifier.fillMaxSize().background(DarkNavy)) {

                                    BarraFiltriUnita(
                                        filtroAttuale = filtroStato,
                                        onFiltroCambiato = { nuovoStato -> viewModel.impostaFiltroStato(nuovoStato) }
                                    )

                                    if (isLoadingPrenotazioni) {
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = AccentBlue)
                                        }
                                    } else if (prenotazioni.isEmpty()) {
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Text("Nessuna prenotazione trovata.", color = Color.Gray)
                                        }
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            items(prenotazioni, key = { it.id }) { prenotazione ->
                                                Surface(
                                                    color = CardOverlay,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                                        Text("Utente: ${prenotazione.viaggiatoreUsername ?: "Sconosciuto"}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                        Text("Viaggio: ${prenotazione.viaggioTitolo}", color = Color.LightGray, fontSize = 12.sp)

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        val (coloreBadge, testoBadge) = when (prenotazione.stato) {
                                                            "CONFERMATA" -> SuccessGreen to "CONFERMATA"
                                                            "ANNULLATA" -> DangerRed to "ANNULLATA"
                                                            else -> Color.Gray to prenotazione.stato
                                                        }

                                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(coloreBadge.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                                            Text(testoBadge, color = coloreBadge, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                            colors = ButtonDefaults.buttonColors(containerColor = if (paginaCorrente > 0) AccentBlue else Color.Gray)
                                        ) { Text("Prec", fontSize = 14.sp) }

                                        Text(text = "Pagina ${paginaCorrente + 1} di ${totalePagine.coerceAtLeast(1)}", color = WhiteText, fontWeight = FontWeight.Medium)

                                        Button(
                                            onClick = { viewModel.caricaPrenotazioniOrganizzatore(paginaCorrente + 1) },
                                            enabled = paginaCorrente < totalePagine - 1,
                                            colors = ButtonDefaults.buttonColors(containerColor = if (paginaCorrente < totalePagine - 1) AccentBlue else Color.Gray)
                                        ) { Text("Succ", fontSize = 14.sp) }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    StatisticheOrganizzatoreScreen(viewModel = viewModelStatistiche)
                }

                2 -> {
                    // SEZIONE MESSAGGI
                    if (identificativoStanzaSelezionata == null) {
                        if (listaDelleStanzeReali.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nessuna conversazione attiva al momento.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listaDelleStanzeReali) { stanzaCorrente ->
                                    Surface(
                                        color = CardOverlay, // Colore scuro
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
                                                Text(stanzaCorrente.titoloDelViaggio, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WhiteText)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Viaggiatore: ${stanzaCorrente.nomeUtenteViaggiatore}", fontSize = 14.sp, color = Color.LightGray)
                                            }

                                            if (stanzaCorrente.numeroMessaggiNonLetti > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = DangerRed,
                                                    modifier = Modifier.padding(start = 8.dp).size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(stanzaCorrente.numeroMessaggiNonLetti.toString(), color = WhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                onClick = { identificativoStanzaSelezionata = null },
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            ) { Text(text = "⬅ Torna alla lista delle chat", color = AccentBlue) }

                            SchermataDellaChat(
                                modelloDiVistaChat = modelloDiVistaChat,
                                identificativoDellaStanza = identificativoStanzaSelezionata!!,
                                nomeDelMittenteLocale = nomeUtente
                            )
                        }
                    }
                }
            }
        }
    }
}

// COMPONENTE: BARRA FILTRI SEGMENTED
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
            .background(CardOverlay)
    ) {
        opzioni.forEach { (valore, etichetta) ->
            val isSelected = filtroAttuale == valore
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) AccentBlue else Color.Transparent)
                    .clickable { onFiltroCambiato(valore) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etichetta,
                    color = if (isSelected) DarkNavy else Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}