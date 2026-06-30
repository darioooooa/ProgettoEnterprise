package com.example.enterprisemobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.MiePrenotazioniViewModel
import com.example.enterprisemobile.model.PrenotazioneEntity
import com.example.enterprisemobile.ui.components.EnterpriseScaffold

class MiePrenotazioniActivity : ComponentActivity() {
    private val viewModel: MiePrenotazioniViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                MiePrenotazioniContent(viewModel)
            }
        }
    }
}

@Composable
fun MiePrenotazioniContent(viewModel: MiePrenotazioniViewModel) {
    val context = LocalContext.current
    val inProgramma by viewModel.prenotazioniInProgramma.collectAsState()
    val completate by viewModel.prenotazioniCompletate.collectAsState()

    EnterpriseScaffold(
        titolo = "I MIEI VIAGGI",
        nomeUtente = viewModel.nomeUtente,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(DarkNavy).padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("I MIEI VIAGGI", color = WhiteText, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gestisci le tue avventure e i dettagli delle prenotazioni", color = Color.Gray, fontSize = 14.sp)
            }

            val tabs = listOf("In programma (${inProgramma.size})", "Timeline impegni", "Completati (${completate.size})")
            TabRow(
                selectedTabIndex = viewModel.tabSelezionata,
                containerColor = CardOverlay,
                contentColor = WhiteText,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[viewModel.tabSelezionata]),
                        color = Color(0xFF10b981),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, titolo ->
                    Tab(
                        selected = viewModel.tabSelezionata == index,
                        onClick = { viewModel.tabSelezionata = index },
                        text = { Text(titolo, fontWeight = if (viewModel.tabSelezionata == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (viewModel.isCaricamento) {
                    CircularProgressIndicator(color = SuccessGreen, modifier = Modifier.align(Alignment.Center))
                } else {
                    when (viewModel.tabSelezionata) {
                        0 -> if (inProgramma.isEmpty()) ViaggiCompletatiVuoti("Nessun viaggio in programma.") else ListaInProgramma(inProgramma)
                        1 -> if (inProgramma.isEmpty()) ViaggiCompletatiVuoti("Nessun impegno in timeline.") else TimelineImpegni(inProgramma)
                        2 -> if (completate.isEmpty()) ViaggiCompletatiVuoti("Non hai ancora completato nessun viaggio.") else ListaInProgramma(completate)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaInProgramma(prenotazioni: List<PrenotazioneEntity>) {
    val context = LocalContext.current

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(prenotazioni) { pren ->
            val coloreStato = when (pren.stato) {
                "CONFERMATA" -> StatusConfirmed
                "ANNULLATA" -> StatusCancelled
                else -> StatusPending
            }

            Surface(color = CardOverlay,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        val idPassato = pren.viaggioId
                        if (idPassato > 0) {
                            val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                                putExtra("VIAGGIO_ID", idPassato)
                            }
                            context.startActivity(intent)
                        } else {
                            android.widget.Toast.makeText(context, "Errore: id viaggio non valido", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#PREN-${pren.id}", color = Color.Gray, fontSize = 12.sp)
                        Text(pren.stato.replace("_", " "), color = coloreStato, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(pren.viaggioTitolo, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📅 Date: ${pren.viaggioDataInizio} - ${pren.viaggioDataFine}", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TimelineImpegni(prenotazioni: List<PrenotazioneEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("Visualizza la cronologia dei tuoi impegni e scopri i periodi in cui sei libero.", color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        }
        items(prenotazioni) { pren ->
            val coloreStato = when (pren.stato) {
                "CONFERMATA" -> Color(0xFF10b981)
                "ANNULLATA" -> Color(0xFFEF5350)
                else -> Color(0xFFFFA726)
            }

            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(modifier = Modifier.width(40.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Canvas(modifier = Modifier.fillMaxHeight().padding(top = 24.dp)) {
                        drawLine(color = Color.Gray, start = Offset(size.width / 2, 0f), end = Offset(size.width / 2, size.height), strokeWidth = 2f)
                    }
                    Box(modifier = Modifier.size(16.dp).background(coloreStato, CircleShape).align(Alignment.TopCenter))
                }

                Surface(color = CardOverlay, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(pren.viaggioTitolo, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Occupato dal ${pren.viaggioDataInizio} al ${pren.viaggioDataFine}", color = Color.LightGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = coloreStato.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, coloreStato)) {
                            Text(pren.stato.replace("_", " "), color = coloreStato, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViaggiCompletatiVuoti(messaggio: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(messaggio, color = Color.LightGray, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}