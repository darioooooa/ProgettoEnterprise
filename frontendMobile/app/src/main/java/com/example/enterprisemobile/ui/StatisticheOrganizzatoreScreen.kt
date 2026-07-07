package com.example.enterprisemobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.RigaViaggioStat
import com.example.enterprisemobile.viewmodels.StatisticheOrganizzatoreViewModel

@Composable
fun StatisticheOrganizzatoreScreen(viewModel: StatisticheOrganizzatoreViewModel) {
    var mostraTuttiViaggi by remember { mutableStateOf(false) }

    val viaggiDaMostrare = if (mostraTuttiViaggi) {
        viewModel.viaggiRecenti
    } else {
        viewModel.viaggiRecenti.take(5)
    }

    val totalePostiVenduti = viaggiDaMostrare.sumOf { it.postiVenduti }
    val totaleRicavo = viaggiDaMostrare.sumOf { it.ricavo }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intestazione
        item {
            Text(
                "Le tue Statistiche 📊",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Monitora l'andamento dei tuoi viaggi e i ricavi.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Le 3 schede riassuntive
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStatCard(
                    titolo = "Viaggi",
                    valore = if (viewModel.isLoading) "..." else viewModel.totaleViaggi.toString(),
                    icona = "✈️",
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    titolo = "Recensioni",
                    valore = if (viewModel.isLoading) "..." else viewModel.totaleRecensioni.toString(),
                    icona = "💬",
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    titolo = "Media",
                    valore = if (viewModel.isLoading) "..." else "${viewModel.mediaRecensioni} / 5",
                    icona = "⭐",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sezione Guadagni con Filtri
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ricavi Totali",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val guadagnoMostrato = viewModel.guadagni[viewModel.filtroGuadagni] ?: 0.0
                    Text(
                        text = if (viewModel.isLoading) "Calcolo in corso..." else "€ ${String.format("%.2f", guadagnoMostrato)}",
                        color = SuccessGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("SETTIMANA", "MESE", "ANNO", "TOTALE").forEach { filtro ->
                            val selezionato = viewModel.filtroGuadagni == filtro
                            Text(
                                text = filtro,
                                color = if (selezionato) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        if (selezionato) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.filtroGuadagni = filtro }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Header sezione viaggi con bottone "Vedi tutti"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ultimi Viaggi Creati",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (viewModel.viaggiRecenti.size > 5) {
                    TextButton(onClick = { mostraTuttiViaggi = !mostraTuttiViaggi }) {
                        Text(
                            text = if (mostraTuttiViaggi) "Mostra meno" else "Vedi tutti (${viewModel.viaggiRecenti.size})",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (viewModel.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (viewModel.viaggiRecenti.isEmpty()) {
            item {
                Text(
                    "Non hai ancora pubblicato nessun viaggio.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(viaggiDaMostrare) { viaggio ->
                ViaggioRigaCard(viaggio)
            }

            // Footer con totali (visibile solo quando espanso)
            if (mostraTuttiViaggi) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TOTALE",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "€ ${String.format("%.2f", totaleRicavo)}",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "${totalePostiVenduti} pax",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(titolo: String, valore: String, icona: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icona, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                valore,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                titolo,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ViaggioRigaCard(viaggio: RigaViaggioStat) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    viaggio.titolo,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Partenza: ${viaggio.data}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "€ ${String.format("%.2f", viaggio.ricavo)}",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "${viaggio.postiVenduti} pax",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}