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
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.viewmodels.HomeOrganizzatoreViewModel
import com.example.enterprisemobile.viewmodels.ViewModelFactory
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation

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

@OptIn(ExperimentalMaterial3Api::class, MapboxExperimental::class)
@Composable
fun SchermataOrganizzatore(nomeUtente: String) {

    val context = LocalContext.current
    val apiService = RetrofitClient.ottieniViaggioService(context)

    //recupero dell'istanza del db e relativo DAO
    val database = AppDatabase.getInstance(context)
    val viaggioDao = database.viaggioDao()
    val repository = ViaggioRepository(apiService,viaggioDao)

    val viewModel: HomeOrganizzatoreViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )

    val viaggi by viewModel.viaggi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    //AVVIO CHIAMATA DI RETE
    LaunchedEffect(Unit) {
        viewModel.caricaDatiMappa()
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

            Text(text = "I Tuoi Itinerari", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Mappa Mapbox
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(12.4964, 41.9028)) // Roma centrale
                    zoom(5.0)
                }
            }

            // Convertiamo il Vector(viaggio_marker dentro res) in Bitmap e lo "ricordiamo" per non ricaricarlo a ogni frame
            val markerIcon = remember(context) {
                ContextCompat.getDrawable(context, R.drawable.viaggio_marker)?.toBitmap()
            }

            //serve per tenere in memoria il viaggio cliccato per poi spedirci nella schemrata dettaglio
            var viaggioSelezionato by remember { mutableStateOf<com.example.enterprisemobile.model.ViaggioMappaDTO?>(null) }

            // Mappa Mapbox racchiusa in un Box per gestire la grafica sovrapposta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                MapboxMap(
                    Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState
                ) {
                    // disegno dei marker
                    viaggi.forEach { viaggio ->
                        PointAnnotation(
                            point = Point.fromLngLat(viaggio.longitudine, viaggio.latitudine),
                            iconImageBitmap = markerIcon,
                            onClick = {
                                //Quando l'utente tocca il segnalino,salviamo il viaggio
                                viaggioSelezionato = viaggio
                                true
                            }
                        )
                    }
                }

                // Rotellina di caricamento mentre scarica da Spring Boot
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viaggioSelezionato?.let { viaggio ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter) // Si aggancia in basso al centro della mappa
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
                                Text(text = viaggio.titolo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Clicca per i dettagli", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    //quando verrà creato la DettaglioViaggioActivity scommentare questa parte di codice
                                    /*
                                    val intent = Intent(context, DettaglioViaggioActivity::class.java)
                                    intent.putExtra("CHIAVE_ID_VIAGGIO", viaggio.id)
                                    context.startActivity(intent)
                                    */

                                    // Chiudiamo il pop-up pulendo lo stato
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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