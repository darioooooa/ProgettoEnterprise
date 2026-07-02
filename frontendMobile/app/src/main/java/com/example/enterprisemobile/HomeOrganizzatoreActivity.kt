package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.viewmodels.HomeOrganizzatoreViewModel
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

        MapboxOptions.accessToken="pk.eyJ1IjoibG9sbG8xOSIsImEiOiJjbXAzNzhuMDAwMmxzMnJzZDh5azZ6ajRpIn0.pYRkM98DgyohuPpF3pf_cQ"

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
    onMarkerClick: (ViaggioMappaDTO) -> Unit
) {
    android.util.Log.d("MAPBOX_PERF", "Rendering  del componente MapboxMap")

    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState
    ) {
        android.util.Log.d("MAPBOX_PERF", "Disegno dei marker per ${viaggi.size} viaggi")

        viaggi.forEach { viaggio ->
            key(viaggio.id) { // Protegge i marker individuali dal ricrearsi inutilmente
                PointAnnotation(
                    point = Point.fromLngLat(viaggio.longitudine, viaggio.latitudine),
                    iconImageBitmap = markerIcon,
                    onClick = {
                        onMarkerClick(viaggio)
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

    val viaggi by viewModel.viaggi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Caricamento asincrono iniziale
    LaunchedEffect(Unit) {
        viewModel.caricaDatiMappa()

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

    EnterpriseScaffold(
        titolo = "Dashboard",
        nomeUtente = nomeUtente,
        //serve per evitare che si bugghi l'ingrandimento della mappa
        gesturesEnabled = false
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text(text = "I tuoi itinerari", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val mapViewportState = rememberMapViewportState()

            LaunchedEffect(Unit) {
                android.util.Log.d("MAPBOX_PERF", "Configurazione iniziale camera")
                mapViewportState.setCameraOptions {
                    center(Point.fromLngLat(12.4964, 41.9028)) // Centrato su Roma
                    zoom(5.0)
                }
            }

            // Convertiamo il Vector(viaggio_marker dentro res) in Bitmap e lo "ricordiamo" per non ricaricarlo a ogni frame
            val markerIcon = remember(context) {
                ContextCompat.getDrawable(context, R.drawable.viaggio_marker)?.toBitmap()
            }

            var viaggioSelezionato by remember { mutableStateOf<ViaggioMappaDTO?>(null) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Iniezione del componente statico isolato
                MappaItinerari(
                    viaggi = viaggi,
                    mapViewportState = mapViewportState,
                    markerIcon = markerIcon,
                    onMarkerClick = { viaggio -> viaggioSelezionato = viaggio }
                )

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viaggioSelezionato?.let { viaggio ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = viaggio.titolo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Clicca per i dettagli",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(context, DettaglioViaggioActivity::class.java)
                                    intent.putExtra("VIAGGIO_ID", viaggio.id)
                                    context.startActivity(intent)

                                    viaggioSelezionato = null
                                }
                            ) {
                                Text("Visualizza")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Azioni Rapide", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

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