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
import androidx.compose.material.icons.filled.ExitToApp // Aggiunto per l'icona di Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.data.security.SessionManager
import com.mapbox.geojson.Point
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataOrganizzatore(nomeUtente: String) {

    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Organizzatore", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        sessionManager.cancellaSessione()

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Benvenuto, $nomeUtente",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mappa Mapbox
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(12.4964, 41.9028)) // Roma
                    zoom(5.0)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                MapboxMap(
                    Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState
                )
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
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icona, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = titolo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}