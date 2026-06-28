package com.example.enterprisemobile

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.data.model.AmiciziaDTO
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.AmiciziaViewModel

class AmiciziaActivity : ComponentActivity() {
    private val viewModel: AmiciziaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                AmiciziaContent(viewModel)
            }
        }
    }
}

@Composable
fun AmiciziaContent(viewModel: AmiciziaViewModel) {
    val context = LocalContext.current
    var tabSelezionata by remember { mutableIntStateOf(0) }
    var amicoDaRimuovere by remember { mutableStateOf<Long?>(null) }

    val tabs = listOf("Amici (${viewModel.listaAmici.size})", "Ricevute (${viewModel.richiesteRicevute.size})", "Inviate (${viewModel.richiesteInviate.size})", "Cerca")

    LaunchedEffect(viewModel.messaggioAvviso) {
        viewModel.messaggioAvviso?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.pulisciMessaggio()
        }
    }


    if (amicoDaRimuovere != null) {
        AlertDialog(
            onDismissRequest = { amicoDaRimuovere = null },
            title = { Text("Rimuovi Amico", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler rimuovere questo utente dai tuoi amici?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rimuoviAmico(amicoDaRimuovere!!)
                        amicoDaRimuovere = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Sì, rimuovi", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { amicoDaRimuovere = null }) {
                    Text("Annulla", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }

    EnterpriseScaffold(
        titolo = "AMICI",
        nomeUtente = viewModel.mioUsername,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(DarkNavy).padding(innerPadding)) {

            TabRow(
                selectedTabIndex = tabSelezionata,
                containerColor = CardOverlay,
                contentColor = WhiteText,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(tabPositions[tabSelezionata]), color = AccentBlue, height = 3.dp)
                }
            ) {
                tabs.forEachIndexed { index, titolo ->
                    Tab(
                        selected = tabSelezionata == index,
                        onClick = { tabSelezionata = index },
                        text = { Text(titolo, fontWeight = if (tabSelezionata == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (viewModel.isLoading && tabSelezionata != 3) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentBlue)
                } else {
                    when (tabSelezionata) {
                        0 -> if (viewModel.listaAmici.isEmpty()) NessunDato("Nessun amico trovato.") else ListaAmici(viewModel.listaAmici, viewModel.mioUsername) { id -> amicoDaRimuovere = id }
                        1 -> if (viewModel.richiesteRicevute.isEmpty()) NessunDato("Nessuna richiesta ricevuta.") else ListaRicevute(viewModel.richiesteRicevute, viewModel)
                        2 -> if (viewModel.richiesteInviate.isEmpty()) NessunDato("Nessuna richiesta inviata.") else ListaInviate(viewModel.richiesteInviate)
                        3 -> CercaAmici(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaAmici(lista: List<AmiciziaDTO>, mioUsername: String, onRimuoviClick: (Long) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { relazione ->
            val nomeAmico = if (relazione.richiedenteUsername == mioUsername) relazione.riceventeUsername else relazione.richiedenteUsername
            val idAmico = if (relazione.richiedenteUsername == mioUsername) relazione.riceventeId else relazione.richiedenteId

            RigaUtente(nomeUtente = nomeAmico) {
                // Invece di rimuovere subito, attiviamo la funzione che apre il popup
                Button(onClick = { onRimuoviClick(idAmico) }, colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) {
                    Text("Rimuovi", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ListaRicevute(lista: List<AmiciziaDTO>, viewModel: AmiciziaViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { richiesta ->
            RigaUtente(nomeUtente = richiesta.richiedenteUsername) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.rifiutaRichiesta(richiesta.id) }, modifier = Modifier.background(DangerRed, CircleShape).size(40.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Rifiuta", tint = WhiteText)
                    }
                    IconButton(onClick = { viewModel.accettaRichiesta(richiesta.id) }, modifier = Modifier.background(SuccessGreen, CircleShape).size(40.dp)) {
                        Icon(Icons.Filled.Check, contentDescription = "Accetta", tint = WhiteText)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaInviate(lista: List<AmiciziaDTO>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { richiesta ->
            RigaUtente(nomeUtente = richiesta.riceventeUsername) {
                Text("In attesa...", color = WarningOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CercaAmici(viewModel: AmiciziaViewModel) {
    var testoRicerca by remember { mutableStateOf("") }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = testoRicerca,
                onValueChange = { testoRicerca = it },
                placeholder = { Text("Inserisci username...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText),
                singleLine = true
            )
            Button(
                onClick = {
                    viewModel.cercaUtente(testoRicerca)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Cerca", color = DarkNavy, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MOSTRA LA CARD DELL'UTENTE CERCATO
        if (viewModel.utenteCercato != null) {
            RigaUtente(nomeUtente = viewModel.utenteCercato!!) {
                Button(
                    onClick = {
                        viewModel.inviaRichiesta(viewModel.utenteCercato!!)
                        viewModel.pulisciRicerca()
                        testoRicerca = "" // Svuota l'input dopo l'invio
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkNavy, strokeWidth = 2.dp)
                    } else {
                        Text("Aggiungi", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            NessunDato("Cerca l'username esatto per inviare una richiesta.")
        }
    }
}

@Composable
fun RigaUtente(nomeUtente: String, azioni: @Composable () -> Unit) {
    Surface(color = CardOverlay, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = AccentBlue.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(nomeUtente, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            azioni()
        }
    }
}

@Composable
fun NessunDato(messaggio: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(messaggio, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 32.dp))
    }
}