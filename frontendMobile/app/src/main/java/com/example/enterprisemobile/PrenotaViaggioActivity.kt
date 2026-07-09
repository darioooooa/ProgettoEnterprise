package com.example.enterprisemobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.PrenotaViaggioViewModel
import com.example.enterprisemobile.ui.components.EnterpriseScaffold

class PrenotaViaggioActivity : ComponentActivity() {
    private val viewModel: PrenotaViaggioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viaggioId = intent.getLongExtra("VIAGGIO_ID", -1L)

        setContent {
            val context = LocalContext.current

            EnterpriseMobileTheme {
                LaunchedEffect(viaggioId) {
                    if (viaggioId != -1L) viewModel.caricaDettagliViaggio(viaggioId)
                }

                if (viewModel.prenotazioneCompletata) {
                    LaunchedEffect(Unit) {
                        val intent = Intent(context, PagamentoActivity::class.java).apply {
                            putExtra("ID_PRENOTAZIONE", viewModel.idPrenotazioneCreata ?: -1L)
                            putExtra("IMPORTO", viewModel.prezzoTotale)
                        }
                        context.startActivity(intent)
                        finish()
                    }
                }

                PrenotaViaggioContent(viewModel, viaggioId)
            }
        }
    }
}

@Composable
fun PrenotaViaggioContent(viewModel: PrenotaViaggioViewModel, viaggioId: Long) {
    val viaggio = viewModel.dettagliViaggio
    val context = LocalContext.current

    EnterpriseScaffold(
        titolo = "PRENOTAZIONE",
        nomeUtente = viewModel.nomeUtente,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        if (viaggio == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 1f))
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(viaggio.titolo, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("📍 ${viaggio.destinazione}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                        if (viaggio.dataInizio != null && viaggio.dataFine != null) {
                            Text("📅 Dal ${viaggio.dataInizio} al ${viaggio.dataFine}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                        Text(viaggio.descrizione ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 15.sp, modifier = Modifier.padding(top = 16.dp))
                    }
                }

                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Riepilogo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 30.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Partecipanti:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { viewModel.diminuisciPersone() }) { Text("-", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp) }
                                Text("${viewModel.numeroPersone}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                TextButton(onClick = { viewModel.aumentaPersone() }) { Text("+", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp) }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Totale:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("€${viewModel.prezzoTotale}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.apriModale() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text("PRENOTA ORA", fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (viewModel.mostraModaleConferma) {
            Dialog(onDismissRequest = { if (!viewModel.isLoading) viewModel.chiudiModale() }) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Conferma Prenotazione", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Vuoi confermare la prenotazione per ${viewModel.numeroPersone} persone?", color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (viewModel.messaggioErrore.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(viewModel.messaggioErrore, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { viewModel.chiudiModale() }, enabled = !viewModel.isLoading) {
                                Text("Annulla", color = MaterialTheme.colorScheme.outline)
                            }
                            Button(
                                onClick = { viewModel.confermaPrenotazione(viaggioId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                enabled = !viewModel.isLoading
                            ) {
                                if (viewModel.isLoading) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Sì, prenota")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}