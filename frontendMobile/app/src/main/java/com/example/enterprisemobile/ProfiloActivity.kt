package com.example.enterprisemobile

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.ProfiloViewModel
import java.time.LocalDate

class ProfiloActivity : ComponentActivity() {
    private val viewModel: ProfiloViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lettura dell'id dall'intent, se non c'è nulla si passa -1L come default (profilo personale)
        val idUtenteRicevuto = intent.getLongExtra("CHIAVE_DETTAGLIO_UTENTE_ID", -1L)
        viewModel.avviaCaricamentoProfilo(idUtenteRicevuto)

        setContent {
            EnterpriseMobileTheme {
                ProfiloContent(viewModel)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.rinfrescaDati()
    }
}

@Composable
fun ProfiloContent(viewModel: ProfiloViewModel) {
    val context = LocalContext.current
    val profilo = viewModel.datiProfilo

    EnterpriseScaffold(
        titolo = if (viewModel.isMioProfilo) "IL MIO PROFILO" else "PROFILO UTENTE",
        nomeUtente = viewModel.sessionManager.ottieniUsername() ?: "Utente",
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 1f)).padding(innerPadding)) {

            if (viewModel.isCaricamento) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else if (profilo != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Sezione intestazione avatar
                    item {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(100.dp)
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(profilo.nomeCompleto ?: "${profilo.nome} ${profilo.cognome}", color = MaterialTheme.colorScheme.onBackground, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text("@${profilo.username}", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }

                    // Schede dei dati anagrafici
                    item {
                        SchedaDettaglio(icona = Icons.Filled.Email, titolo = "Email", valore = profilo.email)
                    }
                    item {
                        SchedaDettaglio(icona = Icons.Filled.Badge, titolo = "Ruolo", valore = profilo.ruolo?.replace("ROLE_", "") ?: "Non definito")
                    }

                    // Sezione dei viaggi creati che compare solo se l'utente è un organizzatore
                    if (profilo.ruolo == "ROLE_ORGANIZZATORE") {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Viaggi organizzati",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        if (viewModel.listaViaggiOrganizzati.isEmpty()) {
                            item {
                                Text(
                                    text = "Nessun viaggio organizzato disponibile.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    textAlign = TextAlign.Start
                                )
                            }
                        } else {
                            // Ciclo dinamico che crea una card per ogni viaggio
                            items(
                                items = viewModel.listaViaggiOrganizzati,
                                key = { viaggio -> viaggio.id ?: 0L }
                            ) { viaggio ->
                                CardViaggioProfilo(viaggio = viaggio)
                            }
                        }
                    }
                }
            } else {
                Text(viewModel.messaggioErrore ?: "Errore sconosciuto", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SchedaDettaglio(icona: ImageVector, titolo: String, valore: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icona, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titolo, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
                Text(valore, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Card per mostrare il singolo itinerario dell'organizzatore
@Composable
fun CardViaggioProfilo(viaggio: ViaggioDTO) {
    val context = LocalContext.current

    // Funzione per verificare se la data del viaggio è nel futuro
    val isFuturo = remember(viaggio.dataInizio) {
        try {
            // Rimuove eventuali frazioni di tempo per estrarre solo la data YYYY-MM-DD
            val pulita = viaggio.dataInizio.substringBefore("T")
            val dataViaggio = LocalDate.parse(pulita)
            dataViaggio.isAfter(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val idPassato = viaggio.id
                if (idPassato != null && idPassato > 0) {
                    val intent = Intent(context, DettaglioViaggioActivity::class.java).apply {
                        putExtra("VIAGGIO_ID", idPassato)
                    }
                    context.startActivity(intent)
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Errore: id viaggio non valido",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viaggio.titolo,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "€ ${viaggio.prezzo}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Destinazione
                Text(text = "📍 ${viaggio.destinazione}", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)

                // Badge e media recensioni del viaggio
                val media = viaggio.mediaRecensioni

                if (media > 0.0) {
                    Text(
                        text = "⭐ ${String.format("%.1f", media)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    if (isFuturo) {
                        Text(
                            text = "Nuovo",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Senza voti",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}