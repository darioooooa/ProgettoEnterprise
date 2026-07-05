package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.ViaggioViewModel
import com.example.enterprisemobile.viewmodels.ItinerarioViewModel
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.ItinerariScreen

class HomeViaggiatoreActivity : ComponentActivity() {
    private val viaggioViewModel: ViaggioViewModel by viewModels()
    private val itinerarioViewModel: ItinerarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                HomeViaggiatoreContent(viaggioViewModel, itinerarioViewModel)
            }
        }
    }
}

@Composable
fun HomeViaggiatoreContent(viewModel: ViaggioViewModel, itinerarioViewModel: ItinerarioViewModel) {
    val listaViaggi by viewModel.viaggiSalvati.collectAsState()
    val mieiItinerari by itinerarioViewModel.itinerari.collectAsState()
    val isItinerarioLoading by itinerarioViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var destinazione by rememberSaveable { mutableStateOf("") }
    var dataMin by rememberSaveable { mutableStateOf("") }
    var dataMax by rememberSaveable { mutableStateOf("") }
    var posti by rememberSaveable { mutableStateOf("") }
    var prezzoMin by rememberSaveable { mutableStateOf("0") }
    var prezzoMax by rememberSaveable { mutableStateOf("5000") }
    var mostraAvanzati by rememberSaveable { mutableStateOf(false) }
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var viaggioSelezionatoId by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostraModaleSceltaItinerario by rememberSaveable { mutableStateOf(false) }

    val items = listOf("Home", "Itinerari", "Messaggi")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Place, Icons.Filled.Email)
    var notificheAmici by remember { mutableIntStateOf(0) }

    // Sincronizza gli itinerari del viaggiatore all'avvio
    LaunchedEffect(Unit) {
        itinerarioViewModel.caricaItinerari()
    }

    EnterpriseScaffold(
        titolo = "ENTERPRISE",
        nomeUtente = viewModel.nomeUtente,
        mostraFrecciaIndietro = false,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
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
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    SearchInput("Destinazione", destinazione) { destinazione = it }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SearchInput("Dal", dataMin, Modifier.weight(1f)) { dataMin = it }
                                        SearchInput("Al", dataMax, Modifier.weight(1f)) { dataMax = it }
                                    }

                                    SearchInput("Posti minimi", posti) { posti = it }

                                    TextButton(onClick = { mostraAvanzati = !mostraAvanzati }) {
                                        Text(
                                            text = if (mostraAvanzati) "🔼 Nascondi filtri prezzo" else "🔽 Mostra filtri prezzo",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (mostraAvanzati) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SearchInput("Min €", prezzoMin, Modifier.weight(1f)) { prezzoMin = it }
                                            SearchInput("Max €", prezzoMax, Modifier.weight(1f)) { prezzoMax = it }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { viewModel.cercaViaggi(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax, 0) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) { Text("Cerca", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }

                        if (viewModel.ricercaEffettuata) {
                            if (listaViaggi.isEmpty()) {
                                item {
                                    Text("Nessun viaggio trovato.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp))
                                }
                            } else {
                                items(listaViaggi) { viaggio ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp),
                                        tonalElevation = 2.dp,
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable {
                                                val idPassato = viaggio.id
                                                if (idPassato > 0) {
                                                    val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                                                        putExtra("VIAGGIO_ID", idPassato)
                                                    }
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "Errore: id viaggio non valido", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(viaggio.titolo, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("🌍 ${viaggio.destinazione}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (viaggio.dataInizio != null && viaggio.dataFine != null) {
                                                    Text("📅 ${viaggio.dataInizio} / ${viaggio.dataFine}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                                Text("💰 ${viaggio.prezzo} €", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Pulsante + per salvare l'itinerario
                                                IconButton(
                                                    onClick = {
                                                        viaggioSelezionatoId = viaggio.id
                                                        mostraModaleSceltaItinerario = true
                                                    },
                                                    modifier = Modifier.padding(end = 4.dp)
                                                ) {
                                                    Icon(Icons.Filled.Add, contentDescription = "Aggiungi a itinerario", tint = MaterialTheme.colorScheme.primary)
                                                }

                                                Button(
                                                    onClick = {
                                                        val intent = Intent(context, PrenotaViaggioActivity::class.java)
                                                        intent.putExtra("VIAGGIO_ID", viaggio.id)
                                                        context.startActivity(intent)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary)
                                                ) {
                                                    Text("Prenota", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (viewModel.ricercaEffettuata) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Button(onClick = { viewModel.cercaViaggi(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax, viewModel.paginaCorrente - 1) }, enabled = viewModel.paginaCorrente > 0) { Text("Prec") }
                                    Text(text = "Pagina ${viewModel.paginaCorrente + 1} di ${viewModel.totalePagine.coerceAtLeast(1)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                    Button(onClick = { viewModel.cercaViaggi(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax, viewModel.paginaCorrente + 1) }, enabled = viewModel.paginaCorrente < viewModel.totalePagine - 1) { Text("Succ") }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    ItinerariScreen(viewModel = itinerarioViewModel)
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Messaggi in arrivo...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
                    }
                }
            }
        }
    }

    // Dialog di selezione dell'itinerario personale
    if (mostraModaleSceltaItinerario && viaggioSelezionatoId != null) {
        AlertDialog(
            onDismissRequest = { mostraModaleSceltaItinerario = false },
            title = { Text("Salva nei tuoi itinerari", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Seleziona una delle tue liste di viaggio:")
                    if (mieiItinerari.isEmpty()) {
                        Text("Non hai ancora creato nessun itinerario personale.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mieiItinerari) { itn ->
                                val giaPresente = itn.viaggiContenuti?.any { it.id == viaggioSelezionatoId } == true
                                Surface(
                                    color = if (giaPresente) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isItinerarioLoading) {
                                            if (giaPresente) {
                                                // Impedisce l'aggiunta duplicata
                                                Toast.makeText(context, "Questo viaggio è già presente in ${itn.nome}!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                itinerarioViewModel.aggiungiViaggioAItinerario(itn.idItinerario ?: 0L, viaggioSelezionatoId!!) { successo ->
                                                    if (successo) {
                                                        Toast.makeText(context, "Viaggio aggiunto con successo!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Errore durante l'aggiunta.", Toast.LENGTH_SHORT).show()
                                                    }
                                                    mostraModaleSceltaItinerario = false
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(itn.nome, fontWeight = FontWeight.Medium)
                                        if (giaPresente) {
                                            Text("Già incluso", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isItinerarioLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = { mostraModaleSceltaItinerario = false }) { Text("Chiudi") }
                }
            }
        )
    }
}

@Composable
fun SearchInput(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true
    )
}