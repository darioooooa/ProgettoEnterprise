package com.example.enterprisemobile

import android.app.Activity
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.components.SezioneAttivitaViaggioInPage
import com.example.enterprisemobile.ui.components.SezioneGalleriaInPage
import com.example.enterprisemobile.ui.components.SezioneRecensioniViaggioInPage
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.CommunityViewModel
import com.example.enterprisemobile.viewmodels.DettaglioViaggioViewModel
import com.example.enterprisemobile.viewmodels.GalleriaViewModel
import com.example.enterprisemobile.viewmodels.ProgrammaViewModel
import com.example.enterprisemobile.viewmodels.ItinerarioViewModel

class DettaglioViaggioActivity : ComponentActivity() {
    private val viewModel: DettaglioViaggioViewModel by viewModels()
    private val galleriaViewModel: GalleriaViewModel by viewModels()
    private val programmaViewModel: ProgrammaViewModel by viewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    private val itinerarioViewModel: ItinerarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idSelezionato = intent.getLongExtra("VIAGGIO_ID", -1L)

        setContent {
            EnterpriseMobileTheme {
                LaunchedEffect(idSelezionato) {
                    if (idSelezionato != -1L) viewModel.inizializza(idSelezionato)
                }
                DettaglioViaggioContent(viewModel, galleriaViewModel, programmaViewModel, communityViewModel, itinerarioViewModel)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (!viewModel.isLoading) {
            viewModel.caricaDatiCompleti()
        }
    }
}

@Composable
fun DettaglioViaggioContent( viewModel: DettaglioViaggioViewModel, galleriaViewModel: GalleriaViewModel, programmaViewModel: ProgrammaViewModel, communityViewModel: CommunityViewModel, itinerarioViewModel: ItinerarioViewModel) {
    val context = LocalContext.current
    val viaggio = viewModel.viaggioEntity
    val stats = viewModel.statisticheDto

    val mieiItinerari by itinerarioViewModel.itinerari.collectAsState()
    val isItinerarioLoading by itinerarioViewModel.isLoading.collectAsState()
    var mostraModaleSceltaItinerario by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        itinerarioViewModel.caricaItinerari()
    }

    EnterpriseScaffold(
        titolo = "Dettagli viaggio",
        nomeUtente = viewModel.mioUsername,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { paddingValues ->
        if (viewModel.isLoading || viaggio == null) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                // Il colore dell'indicatore userà il colore primario del tema corrente
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Esplora la destinazione",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "📍 ${viaggio.destinazione}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (viewModel.isMioViaggio()) {
                            Button(
                                onClick = { viewModel.mostraDialogEliminazione = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                enabled = !viewModel.isEliminazioneInCorso
                            ) {
                                Text(if (viewModel.isEliminazioneInCorso) "⏳ Rimborsi..." else "🗑️ Elimina")
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = viaggio.titolo ?: "Dettaglio viaggio",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viaggio.descrizione
                                ?: "Nessuna descrizione fornita per questo itinerario.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Alert / notifiche
                viewModel.messaggioAvviso?.let { avviso ->
                    item {
                        val col =
                            if (viewModel.tipoAvviso == "successo") SuccessGreen else MaterialTheme.colorScheme.error
                        Surface(
                            color = col.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    avviso,
                                    color = col,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "×",
                                    color = col,
                                    fontSize = 22.sp,
                                    modifier = Modifier.clickable {
                                        viewModel.messaggioAvviso = null
                                    })
                            }
                        }
                    }
                }

                // Stato prenotazione
                if (!viewModel.isMioViaggio() && viewModel.mioRuolo == "ROLE_VIAGGIATORE") {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (viewModel.isGiaAcquistato) {
                                        val (badgeTesto, badgeColore) = when (viewModel.statoSvolgimentoIscrizione) {
                                            "PRENOTATO" -> "✅ Sei già prenotato" to SuccessGreen
                                            "IN_CORSO" -> "✈️ Viaggio in corso" to MaterialTheme.colorScheme.primary
                                            else -> "🏁 Viaggio completato" to Color.Gray
                                        }
                                        Text(
                                            badgeTesto,
                                            color = badgeColore,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    } else if (!viewModel.isTuttoEsaurito()) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(
                                                    context,
                                                    PrenotaViaggioActivity::class.java
                                                ).apply { putExtra("VIAGGIO_ID", viaggio.id) }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) { Text("Prenota ora") }
                                    } else if (viewModel.isTuttoEsaurito()) {
                                        Text(
                                            "⚠️ Tutto esaurito",
                                            color = Color(0xFFFBBF24),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    } else {
                                        Text(
                                            "🔒 Iscrizioni chiuse",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { mostraModaleSceltaItinerario = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) {
                                        Text("＋ Lista", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.scaricaFileIcs() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    ) { Text("📅 Esporta calendario", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }

                // Periodo e itinerario del viaggio
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "📅 Periodo viaggio",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${viaggio.dataInizio} - ${viaggio.dataFine}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "✈️ Itinerario",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${viaggio.cittaPartenza} ➔ ${viaggio.destinazione}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Quota e posti
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "💰 Quota di partecipazione",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                if (!viewModel.inModificaPrezzo) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable(enabled = viewModel.isMioViaggio()) {
                                            viewModel.nuovoPrezzoInput = viaggio.prezzo.toString()
                                            viewModel.inModificaPrezzo = true
                                        }
                                    ) {
                                        Text(
                                            "€ ${viaggio.prezzo}",
                                            color = if (viewModel.isMioViaggio()) Color(0xFFF59E0B) else SuccessGreen,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (viewModel.isMioViaggio()) {
                                            Text(
                                                " ✏️",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = viewModel.nuovoPrezzoInput,
                                            onValueChange = { viewModel.nuovoPrezzoInput = it },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Button(
                                                onClick = { viewModel.salvaNuovoPrezzo() },
                                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text("✓", fontSize = 12.sp) }

                                            Button(
                                                onClick = { viewModel.inModificaPrezzo = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text("✗", fontSize = 12.sp) }
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "👥 Posti e partecipanti",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${viaggio.partecipantiAttuali} / ${viaggio.maxPartecipanti} occupati",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Valutazioni
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "⭐ Media voti",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val media = stats?.mediaRecensioni ?: 0.0
                                Text(
                                    if (media > 0.0) "$media / 5" else "Nessuna",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "💬 Recensioni totali",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${stats?.numeroRecensioni ?: 0} recensioni",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Card info organizzatore
                stats?.organizzatoreUsername?.let { username ->
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    // Si prende l'id dell'organizzatore dalle statistiche
                                    val idOrganizzatore = stats.organizzatoreId

                                    if (idOrganizzatore != null && idOrganizzatore > 0L) {
                                        val intent = Intent(
                                            context,
                                            ProfiloActivity::class.java
                                        ).apply {
                                            putExtra(
                                                "CHIAVE_DETTAGLIO_UTENTE_ID",
                                                idOrganizzatore
                                            )
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Errore: id organizzatore non disponibile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "👤",
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Column {
                                        Text(
                                            "Organizzatore del viaggio",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            username,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Clicca per vedere il profilo",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Text(
                                    "➔",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                // Sezione galleria
                item(
                    key = "sezione_galleria_swipe",
                    contentType = "GalleriaType"
                ) {
                    SezioneGalleriaInPage(
                        viewModel = galleriaViewModel,
                        viaggioId = viaggio.id,
                        isMioViaggio = viewModel.isMioViaggio(),
                        context = context
                    )
                }

                // Sezione attività
                item(
                    key = "sezione_attivita_lista",
                    contentType = "AttivitaType"
                ) {
                    SezioneAttivitaViaggioInPage(
                        viewModel = programmaViewModel,
                        viaggioId = viaggio.id,
                        isMioViaggio = viewModel.isMioViaggio(),
                        context = context
                    )
                }

                // Sezione recensioni
                item(
                    key = "sezione_recensioni_feed",
                    contentType = "RecensioniType"
                ) {
                    SezioneRecensioniViaggioInPage(
                        viewModel = communityViewModel,
                        viaggioId = viaggio.id,
                        isMioViaggio = viewModel.isMioViaggio(),
                        isGiaAcquistato = viewModel.isGiaAcquistato,
                        statoSvolgimentoIscrizione = viewModel.statoSvolgimentoIscrizione,
                        context = context
                    )
                }

                // Sezione informativa
                if (!viewModel.isMioViaggio() && viewModel.mioRuolo == "ROLE_VIAGGIATORE") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (viewModel.isGiaAcquistato) {
                                    Text(
                                        "La tua avventura è confermata!",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    val stringaStatoBasso =
                                        when (viewModel.statoSvolgimentoIscrizione) {
                                            "PRENOTATO" -> "Hai un posto confermato. Prepara i bagagli! 🧳"
                                            "IN_CORSO" -> "Sei in viaggio! Consulta il Programma per le tappe odierne. 🗺️"
                                            else -> "Il viaggio si è concluso. Lascia un feedback!"
                                        }
                                    Text(
                                        stringaStatoBasso,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                } else if (!viewModel.isTuttoEsaurito()) {
                                    Text(
                                        "Ti piace questo itinerario?",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Assicurati il tuo posto prima che le iscrizioni si esauriscano!",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                context,
                                                PrenotaViaggioActivity::class.java
                                            ).apply { putExtra("VIAGGIO_ID", viaggio.id) }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "Prenota il tuo posto ora",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (viewModel.isTuttoEsaurito()) {
                                    Text(
                                        "Posti esauriti! 👥",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Siamo spiacenti, tutti i posti per questo itinerario sono stati occupati.",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        "Iscrizioni chiuse 🔒",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Purtroppo non è più possibile prenotarsi a questo itinerario.",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (viewModel.mostraDialogEliminazione) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.mostraDialogEliminazione = false
                    }, // Chiude cliccando fuori
                    title = {
                        Text(
                            text = "Conferma eliminazione",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Sei sicuro di voler eliminare definitivamente questo viaggio? Tutti i partecipanti verranno automaticamente rimborsati. Questa azione non può essere annullata.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Chiama l'eliminazione e chiude l'Activity
                                viewModel.eliminaViaggioCorrente {
                                    (context as? Activity)?.finish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Sì, elimina", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.mostraDialogEliminazione = false
                            } // Annulla l'operazione
                        ) {
                            Text("Annulla", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Dialog per salvare il viaggio in una lista personale
            if (mostraModaleSceltaItinerario) {
                AlertDialog(
                    onDismissRequest = { mostraModaleSceltaItinerario = false },
                    title = { Text("Aggiungi all'itinerario", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("In quale delle tue liste vuoi salvare questo viaggio?")
                            if (mieiItinerari.isEmpty()) {
                                Text(
                                    "Nessun itinerario trovato.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 200.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(mieiItinerari) { index, itn ->
                                        val idListaSicuro = itn.idItinerario ?: 0L
                                        val idViaggioSicuro = viaggio?.id ?: 0L
                                        val giaPresente = itn.viaggiContenuti?.any { it.id == idViaggioSicuro } == true

                                        Surface(
                                            color = if (giaPresente) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = !isItinerarioLoading) {
                                                    if (giaPresente) {
                                                        // Impedisce il salvataggio duplicato
                                                        Toast.makeText(context, "Questo viaggio è già presente in ${itn.nome}!", Toast.LENGTH_SHORT).show()
                                                    } else if (idViaggioSicuro > 0L) {
                                                        itinerarioViewModel.aggiungiViaggioAItinerario(
                                                            idLista = idListaSicuro,
                                                            idViaggio = idViaggioSicuro,
                                                            onEsito = { esito ->
                                                                if (esito) {
                                                                    Toast.makeText(context, "Aggiunto con successo!", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "Errore di salvataggio.", Toast.LENGTH_SHORT).show()
                                                                }
                                                                mostraModaleSceltaItinerario = false
                                                            }
                                                        )
                                                    } else {
                                                        Toast.makeText(context, "Impossibile salvare: ID viaggio non valido.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = itn.nome, fontWeight = FontWeight.Medium)
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
                        if (isItinerarioLoading)
                            CircularProgressIndicator(modifier = Modifier.size(24.dp)
                        )
                        else
                            TextButton(onClick = { mostraModaleSceltaItinerario = false }) { Text("Chiudi") }
                    }
                )
            }
        }
    }
}