package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.model.ViaggioMappaDTO
import com.example.enterprisemobile.ui.StatisticheOrganizzatoreScreen
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
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
        val usernameRicevuto = intent.getStringExtra("CHIAVE_USERNAME") ?: "Utente"

        MapboxOptions.accessToken = com.example.enterprisemobile.BuildConfig.MAPBOX_TOKEN

        setContent {
            EnterpriseMobileTheme {
                SchermataOrganizzatore(usernameRicevuto)
            }
        }
    }
}

// Isola il ciclo di vita grafico di Mapbox impedendo recomposition esterne sulla dashboard
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
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState
    ) {
        android.util.Log.d("MAPBOX_PERF", "Disegno di ${viaggiRaggruppati.size} cluster di marker stabili")

        viaggiRaggruppati.forEach { (coordinataChiave, listaViaggiInPunto) ->
            // Estrazione della posizione dal primo viaggio del gruppo
            val primoViaggio = listaViaggiInPunto.first()

            key(coordinataChiave) { // Chiave basata sulle coordinate univoche
                PointAnnotation(
                    point = Point.fromLngLat(primoViaggio.longitudine, primoViaggio.latitudine),
                    iconImageBitmap = markerIcon,
                    onClick = {
                        // Invio dell'intero set di viaggi presenti in questo punto esatto
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
    android.util.Log.d("MAPBOX_PERF", "Recomposition generale della SchermataOrganizzatore")

    val context = LocalContext.current
    val apiService = remember(context) { RetrofitClient.ottieniViaggioService(context) }
    val utenteApiService = remember(context) { RetrofitClient.ottieniUtenteService(context) }

    val repository = remember(context) {
        val database = AppDatabase.getInstance(context)
        ViaggioRepository(apiService, database.viaggioDao())
    }

    val viewModel: HomeOrganizzatoreViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )
    val viewModelStatistiche: StatisticheOrganizzatoreViewModel = viewModel()

    // Inizializzazione della Chat
    val modelloDiVistaChat: ChatViewModel = viewModel(
        factory = GeneratoreChatViewModel(context)
    )
    val listaDelleStanzeReali by modelloDiVistaChat.stanzeVisibili.collectAsState()
    var identificativoStanzaSelezionata by rememberSaveable { mutableStateOf<Long?>(null) }


    val viaggi by viewModel.viaggi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Stato per la bottom bar
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    // Caricamento asincrono iniziale
    LaunchedEffect(Unit) {
        viewModel.caricaDatiMappa()

        // Avviamo il recupero e l'ascolto delle notifiche per l'organizzatore
        modelloDiVistaChat.caricaLeMieStanzeOrganizzatore(nomeUtente)
        modelloDiVistaChat.attivaAscoltoNotificheOrganizzatore(nomeUtente)

        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("FCM_ERROR", "Recupero token fallito", task.exception)
                return@addOnCompleteListener
            }
            val tokenRicevuto = task.result

            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val payload = mapOf("token" to tokenRicevuto)
                    val risposta = utenteApiService.aggiornaToken(payload)
                    if (risposta.isSuccessful) {
                        android.util.Log.d("FCM_SUCCESS", "Token salvato su Spring Boot!")
                    } else {
                        val motivo = risposta.errorBody()?.string()
                        android.util.Log.e("FCM_ERROR", "Errore dal server: ${risposta.code()} - Motivo: $motivo")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FCM_API_ERROR", "Errore invio token al server", e)
                }
            }
        }
    }

    val items = listOf("Home", "Statistiche", "Messaggi")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.BarChart, Icons.Filled.Email)

    // Calcoliamo il numero totale delle notifiche live
    val totaleNotifiche = listaDelleStanzeReali.sumOf { it.numeroMessaggiNonLetti }

    EnterpriseScaffold(
        titolo = "Dashboard",
        nomeUtente = nomeUtente,
        //serve per evitare che si bugghi l'ingrandimento della mappa
        gesturesEnabled = false,

        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            if (index == 2 && totaleNotifiche > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(totaleNotifiche.toString())
                                        }
                                    }
                                ) {
                                    Icon(icons[index], contentDescription = item)
                                }
                            } else {
                                Icon(icons[index], contentDescription = item)
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> {
                    // SEZIONE HOME - Mappa e Azioni Rapide
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(text = "I tuoi itinerari", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        item {
                            val mapViewportState = rememberMapViewportState()

                            LaunchedEffect(Unit) {
                                android.util.Log.d("MAPBOX_PERF", "Configurazione iniziale camera")
                                mapViewportState.setCameraOptions {
                                    center(Point.fromLngLat(12.4964, 41.9028)) // Centrato su Roma
                                    zoom(5.0)
                                }
                            }

                            val markerIcon = remember(context) {
                                ContextCompat.getDrawable(context, R.drawable.viaggio_marker)?.toBitmap()
                            }

                            // Tiene traccia della lista di viaggi cliccati nel marker
                            var viaggiSelezionatiInMarker by remember { mutableStateOf<List<ViaggioMappaDTO>>(emptyList()) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                            ) {
                                // Iniezione del componente statico isolato
                                MappaItinerari(
                                    viaggi = viaggi,
                                    mapViewportState = mapViewportState,
                                    markerIcon = markerIcon,
                                    onMarkerClick = { listaViaggi ->
                                        viaggiSelezionatiInMarker = listaViaggi
                                    }
                                )

                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }

                                if (viaggiSelezionatiInMarker.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(12.dp)
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            // Intestazione popup
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (viaggiSelezionatiInMarker.size > 1)
                                                        "🗺️ ${viaggiSelezionatiInMarker.size} viaggi in questa posizione"
                                                    else "📍 Viaggio in questa posizione",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(
                                                    onClick = { viaggiSelezionatiInMarker = emptyList() },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Chiudi")
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Elenco scrollabile dei viaggi nel marker
                                            Box(modifier = Modifier.heightIn(max = 180.dp)) {
                                                LazyColumn(
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    items(viaggiSelezionatiInMarker.size) { index ->
                                                        val viaggio = viaggiSelezionatiInMarker[index]
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(
                                                                    MaterialTheme.colorScheme.surface,
                                                                    shape = RoundedCornerShape(8.dp)
                                                                )
                                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = viaggio.titolo,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Button(
                                                                onClick = {
                                                                    val intent = Intent(context, DettaglioViaggioActivity::class.java)
                                                                    intent.putExtra("VIAGGIO_ID", viaggio.id)
                                                                    context.startActivity(intent)
                                                                    viaggiSelezionatiInMarker = emptyList()
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(32.dp)
                                                            ) {
                                                                Text("Vedi", fontSize = 12.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(text = "Azioni Rapide", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AzioneCard(
                                    titolo = "Nuovo\nItinerario",
                                    icona = Icons.Default.Add,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val intent = Intent(context, CreaViaggioActivity::class.java)
                                    context.startActivity(intent)
                                }

                                AzioneCard(
                                    titolo = "Gestione\nPrenotazioni",
                                    icona = Icons.AutoMirrored.Filled.List,
                                    modifier = Modifier.weight(1f)
                                ) {}
                            }
                        }
                    }
                }

                1 -> {
                    StatisticheOrganizzatoreScreen(viewModel = viewModelStatistiche)
                }

                2 -> {
                    // SEZIONE MESSAGGI - Chat Completa Organizzatore
                    if (identificativoStanzaSelezionata == null) {
                        if (listaDelleStanzeReali.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nessuna conversazione attiva al momento.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                // Avvisa il server che l'organizzatore ha aperto la chat tenendo la logica separata
                                                modelloDiVistaChat.azzeraNotificheStanzaOrganizzatore(
                                                    stanzaCorrente.identificativoStanza,
                                                    nomeUtente
                                                )
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stanzaCorrente.titoloDelViaggio,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Mostriamo chi è il viaggiatore
                                                Text(
                                                    text = "Viaggiatore: ${stanzaCorrente.nomeUtenteViaggiatore}",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (stanzaCorrente.numeroMessaggiNonLetti > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(start = 8.dp).size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = stanzaCorrente.numeroMessaggiNonLetti.toString(),
                                                            color = MaterialTheme.colorScheme.onError,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
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
                            ) {
                                Text(text = "⬅ Torna alla lista delle chat")
                            }

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzioneCard(titolo: String, icona: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icona, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = titolo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}