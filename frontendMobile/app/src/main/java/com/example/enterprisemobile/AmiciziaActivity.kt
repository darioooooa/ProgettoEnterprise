package com.example.enterprisemobile

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    var tabSelezionata by rememberSaveable { mutableIntStateOf(0) }

    var amicoDaRimuovere by remember { mutableStateOf<Long?>(null) }
    var richiestaDaAccettare by remember { mutableStateOf<Long?>(null) }
    var richiestaDaRifiutare by remember { mutableStateOf<Long?>(null) }

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
            title = { Text("Rimuovi Amico", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Sei sicuro di voler rimuovere questo utente dai tuoi amici?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.rimuoviAmico(amicoDaRimuovere!!); amicoDaRimuovere = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sì, rimuovi", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { amicoDaRimuovere = null }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (richiestaDaAccettare != null) {
        AlertDialog(
            onDismissRequest = { richiestaDaAccettare = null },
            title = { Text("Accetta Richiesta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Vuoi aggiungere questo utente alla tua lista amici?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.accettaRichiesta(richiestaDaAccettare!!); richiestaDaAccettare = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Accetta", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { richiestaDaAccettare = null }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (richiestaDaRifiutare != null) {
        AlertDialog(
            onDismissRequest = { richiestaDaRifiutare = null },
            title = { Text("Rifiuta Richiesta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Sei sicuro di voler rifiutare questa richiesta di amicizia?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.rifiutaRichiesta(richiestaDaRifiutare!!); richiestaDaRifiutare = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Rifiuta", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { richiestaDaRifiutare = null }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (viewModel.amicoSelezionatoPerItinerari != null) {
        Dialog(onDismissRequest = { viewModel.chiudiModaleItinerari() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Itinerari di ${viewModel.amicoSelezionatoPerItinerari}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.isLoadingItinerari) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (viewModel.itinerariAmico.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nessun itinerario pubblico.", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            items(viewModel.itinerariAmico) { itinerario ->

                                var espanso by rememberSaveable { mutableStateOf(false) }

                                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { espanso = !espanso }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(itinerario.nome, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Condivisione: Pubblica", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                            }
                                            Icon(
                                                imageVector = if (espanso) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                contentDescription = "Espandi",
                                                tint = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        if (espanso) {
                                            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(bottom = 8.dp))

                                                if (itinerario.viaggiContenuti.isNullOrEmpty()) {
                                                    Text("Nessun viaggio inserito.", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                                                } else {
                                                    itinerario.viaggiContenuti.forEach { viaggio ->
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    val idPassato = viaggio.id ?: 0L
                                                                    if (idPassato > 0L) {
                                                                        val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                                                                            putExtra("VIAGGIO_ID", idPassato)
                                                                        }
                                                                        context.startActivity(intent)
                                                                    } else {
                                                                        Toast.makeText(context, "Errore: id viaggio non valido", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                                        ) {
                                                            Text("✈️", fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                                                            Column {
                                                                Text(viaggio.titolo ?: "Viaggio sconosciuto", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                                if (!viaggio.destinazione.isNullOrEmpty()) {
                                                                    Text(viaggio.destinazione, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
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
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.chiudiModaleItinerari() },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Chiudi", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    EnterpriseScaffold(
        titolo = "AMICI",
        nomeUtente = viewModel.mioUsername,
        mostraFrecciaIndietro = true,
        badgeAmiciOverride = viewModel.richiesteRicevute.size,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 1f)).padding(innerPadding)) {

            TabRow(
                selectedTabIndex = tabSelezionata,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(tabPositions[tabSelezionata]), color = MaterialTheme.colorScheme.primary, height = 3.dp)
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                } else {
                    when (tabSelezionata) {
                        0 -> if (viewModel.listaAmici.isEmpty()) NessunDato("Nessun amico trovato.") else ListaAmici(viewModel.listaAmici, viewModel.mioUsername, viewModel) { id -> amicoDaRimuovere = id }
                        1 -> if (viewModel.richiesteRicevute.isEmpty()) NessunDato("Nessuna richiesta ricevuta.") else ListaRicevute(viewModel.richiesteRicevute, onAccettaClick = { id -> richiestaDaAccettare = id }, onRifiutaClick = { id -> richiestaDaRifiutare = id })
                        2 -> if (viewModel.richiesteInviate.isEmpty()) NessunDato("Nessuna richiesta inviata.") else ListaInviate(viewModel.richiesteInviate)
                        3 -> CercaAmici(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaAmici(lista: List<AmiciziaDTO>, mioUsername: String, viewModel: AmiciziaViewModel, onRimuoviClick: (Long) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { relazione ->
            val nomeAmico = if (relazione.richiedenteUsername == mioUsername) relazione.riceventeUsername else relazione.richiedenteUsername
            val idAmico = if (relazione.richiedenteUsername == mioUsername) relazione.riceventeId else relazione.richiedenteId

            RigaUtente(nomeUtente = nomeAmico) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { viewModel.caricaItinerariPubbliciAmico(nomeAmico) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Itinerari", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onRimuoviClick(idAmico) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("X", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaRicevute(lista: List<AmiciziaDTO>, onAccettaClick: (Long) -> Unit, onRifiutaClick: (Long) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lista) { richiesta ->
            RigaUtente(nomeUtente = richiesta.richiedenteUsername) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onRifiutaClick(richiesta.id) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.error, CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Rifiuta", tint = MaterialTheme.colorScheme.onError)
                    }
                    IconButton(
                        onClick = { onAccettaClick(richiesta.id) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Accetta", tint = MaterialTheme.colorScheme.onPrimary)
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
                Text("In attesa...", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CercaAmici(viewModel: AmiciziaViewModel) {
    var testoRicerca by rememberSaveable { mutableStateOf("") }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = testoRicerca,
                onValueChange = { testoRicerca = it },
                placeholder = { Text("Inserisci username...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = { viewModel.cercaUtente(testoRicerca) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Cerca", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.utenteCercato != null) {
            RigaUtente(nomeUtente = viewModel.utenteCercato!!) {
                Button(
                    onClick = {
                        viewModel.inviaRichiesta(viewModel.utenteCercato!!)
                        viewModel.pulisciRicerca()
                        testoRicerca = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Aggiungi", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(nomeUtente, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            azioni()
        }
    }
}

@Composable
fun NessunDato(messaggio: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(messaggio, color = MaterialTheme.colorScheme.outline, fontSize = 16.sp, modifier = Modifier.padding(top = 32.dp))
    }
}