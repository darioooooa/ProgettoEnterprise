package com.example.enterprisemobile

import android.app.Activity
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
            EnterpriseMobileTheme {
                LaunchedEffect(viaggioId) {
                    if (viaggioId != -1L) viewModel.caricaDettagliViaggio(viaggioId)
                }

                if (viewModel.prenotazioneCompletata) {
                    LaunchedEffect(Unit) {
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
            Box(modifier = Modifier.fillMaxSize().background(DarkNavy), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkNavy)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(color = CardOverlay, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(viaggio.titolo, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("📍 ${viaggio.destinazione}", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                        Text(viaggio.descrizione ?: "", color = TextSecondary, fontSize = 15.sp, modifier = Modifier.padding(top = 16.dp))
                    }
                }

                Surface(color = CardOverlay, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Riepilogo", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 30.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Partecipanti:", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { viewModel.diminuisciPersone() }) { Text("-", color = WhiteText, fontSize = 20.sp) }
                                Text("${viewModel.numeroPersone}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                TextButton(onClick = { viewModel.aumentaPersone() }) { Text("+", color = WhiteText, fontSize = 20.sp) }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Totale:", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("€${viewModel.prezzoTotale}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.apriModale() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) { Text("PRENOTA ORA", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (viewModel.mostraModaleConferma) {
            Dialog(onDismissRequest = { viewModel.chiudiModale() }) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Conferma Prenotazione", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Vuoi confermare la prenotazione per ${viewModel.numeroPersone} persone?", color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { viewModel.chiudiModale() }) {
                                Text("Annulla", color = Color.Gray)
                            }
                            Button(
                                onClick = { viewModel.confermaPrenotazione(viaggioId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10b981))
                            ) {
                                Text("Sì, prenota")
                            }
                        }
                    }
                }
            }
        }
    }
}