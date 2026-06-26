package com.example.enterprisemobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.ViaggioViewModel

class HomeViaggiatoreActivity : ComponentActivity() {
    private val viewModel: ViaggioViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                HomeViaggiatoreContent(viewModel)
            }
        }
    }
}

@Composable
fun HomeViaggiatoreContent(viewModel: ViaggioViewModel) {
    val listaViaggi by viewModel.viaggiSalvati.collectAsState()

    // Stato Filtri
    var destinazione by remember { mutableStateOf("") }
    var dataMin by remember { mutableStateOf("") }
    var dataMax by remember { mutableStateOf("") }
    var posti by remember { mutableStateOf("") }
    var prezzoMin by remember { mutableStateOf("0") }
    var prezzoMax by remember { mutableStateOf("5000") }
    var mostraAvanzati by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("Home", "Itinerari", "Messaggi")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Place, Icons.Filled.Email)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkNavy) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WhiteText, unselectedIconColor = Color.Gray, indicatorColor = CardOverlay
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(DarkNavy).padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP BAR
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ENTERPRISE", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${viewModel.nomeUtente}", color = WhiteText, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Profilo", tint = WhiteText, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // CARD RICERCA
            item {
                Surface(color = CardOverlay, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        SearchInput("Destinazione", destinazione) { destinazione = it }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SearchInput("Dal", dataMin, Modifier.weight(1f)) { dataMin = it }
                            SearchInput("Al", dataMax, Modifier.weight(1f)) { dataMax = it }
                        }

                        SearchInput("Posti minimi", posti) { posti = it }

                        TextButton(onClick = { mostraAvanzati = !mostraAvanzati }) {
                            Text(if (mostraAvanzati) "🔼 Nascondi filtri prezzo" else "🔽 Mostra filtri prezzo", color = Color.Gray)
                        }

                        if (mostraAvanzati) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SearchInput("Min €", prezzoMin, Modifier.weight(1f)) { prezzoMin = it }
                                SearchInput("Max €", prezzoMax, Modifier.weight(1f)) { prezzoMax = it }
                            }
                        }

                        Button(
                            onClick = { viewModel.cercaViaggi(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax, 0) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) { Text("Cerca", color = DarkNavy, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // LISTA VIAGGI
            if (viewModel.ricercaEffettuata) {
                if (listaViaggi.isEmpty()) {
                    item { Text("Nessun viaggio trovato.", color = Color.Gray, modifier = Modifier.padding(top = 20.dp)) }
                } else {
                    items(listaViaggi) { viaggio ->
                        Surface(color = CardOverlay, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(viaggio.titolo, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("🌍 ${viaggio.destinazione}", color = Color.LightGray, fontSize = 14.sp)
                                Text("💰 ${viaggio.prezzo} €", color = Color.Green, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // PAGINAZIONE
            if (viewModel.ricercaEffettuata) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.cercaViaggi(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax, viewModel.paginaCorrente - 1) },
                            enabled = viewModel.paginaCorrente > 0
                        ) { Text("Prec") }

                        Text(
                            text = "Pagina ${viewModel.paginaCorrente + 1} di ${viewModel.totalePagine.coerceAtLeast(1)}",
                            color = WhiteText,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = { viewModel.cercaViaggi(destinazione, dataMin, dataMax, posti, prezzoMin, prezzoMax, viewModel.paginaCorrente + 1) },
                            enabled = viewModel.paginaCorrente < viewModel.totalePagine - 1
                        ) { Text("Succ") }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchInput(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, color = Color.Gray) },
        modifier = modifier, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText, focusedBorderColor = WhiteText)
    )
}