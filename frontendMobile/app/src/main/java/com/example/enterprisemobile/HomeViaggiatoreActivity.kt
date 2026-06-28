package com.example.enterprisemobile

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
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
    val context = LocalContext.current

    var destinazione by rememberSaveable { mutableStateOf("") }
    var dataMin by rememberSaveable { mutableStateOf("") }
    var dataMax by rememberSaveable { mutableStateOf("") }
    var posti by rememberSaveable { mutableStateOf("") }
    var prezzoMin by rememberSaveable { mutableStateOf("0") }
    var prezzoMax by rememberSaveable { mutableStateOf("5000") }
    var mostraAvanzati by rememberSaveable { mutableStateOf(false) }
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    val items = listOf("Home", "Itinerari", "Messaggi")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Place, Icons.Filled.Email)

    EnterpriseScaffold(
        titolo = "ENTERPRISE",
        nomeUtente = viewModel.nomeUtente,
        mostraFrecciaIndietro = false,
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
        Box(modifier = Modifier.fillMaxSize().background(DarkNavy).padding(innerPadding)) {
            when (selectedItem) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
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

                        if (viewModel.ricercaEffettuata) {
                            if (listaViaggi.isEmpty()) {
                                item { Text("Nessun viaggio trovato.", color = Color.Gray, modifier = Modifier.padding(top = 20.dp)) }
                            } else {
                                items(listaViaggi) { viaggio ->
                                    Surface(
                                        color = CardOverlay,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(viaggio.titolo, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("🌍 ${viaggio.destinazione}", color = Color.LightGray, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (viaggio.dataInizio != null && viaggio.dataFine != null) {
                                                    Text("📅 ${viaggio.dataInizio} / ${viaggio.dataFine}", color = Color.LightGray, fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                                Text("💰 ${viaggio.prezzo} €", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    val intent = Intent(context, PrenotaViaggioActivity::class.java)
                                                    intent.putExtra("VIAGGIO_ID", viaggio.id)
                                                    context.startActivity(intent)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10b981)),
                                                modifier = Modifier.padding(start = 8.dp)
                                            ) {
                                                Text("Prenota Ora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                    Text(text = "Pagina ${viewModel.paginaCorrente + 1} di ${viewModel.totalePagine.coerceAtLeast(1)}", color = WhiteText, fontWeight = FontWeight.Medium)
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
                        Text("Messaggi in arrivo...", color = Color.Gray, fontSize = 18.sp)
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