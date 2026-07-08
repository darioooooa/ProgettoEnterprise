package com.example.enterprisemobile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.repository.ViaggioRepository
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.CreaViaggioState
import com.example.enterprisemobile.viewmodels.CreaViaggioViewModel
import com.example.enterprisemobile.viewmodels.TappaState
import java.util.Calendar
import java.util.Locale

// ===== MAPPA EMOJI PER I TAG =====
val emojiMap = mapOf(
    "Mare" to "🏖️",
    "Montagna" to "🏔️",
    "Città d'arte" to "🎨",
    "Relax" to "🧘",
    "Avventura" to "🏕️",
    "Cultura" to "🏛️",
    "Enogastronomia" to "🍷",
    "Economico" to "💰",
    "Lusso" to "💎",
    "Inverno" to "❄️",
    "Estate" to "☀️"
)

// ===== MAPPA COLORI PER I TAG (adattati al tema scuro) =====
val colorMap = mapOf(
    "Mare" to Color(0xFF60A5FA),         // Blu chiaro
    "Montagna" to Color(0xFF34D399),     // Verde chiaro
    "Città d'arte" to Color(0xFFA78BFA), // Viola chiaro
    "Relax" to Color(0xFFF472B6),        // Rosa chiaro
    "Avventura" to Color(0xFFFB923C),    // Arancione chiaro
    "Cultura" to Color(0xFF818CF8),      // Indaco chiaro
    "Enogastronomia" to Color(0xFFF87171), // Rosso chiaro
    "Economico" to Color(0xFF34D399),    // Verde
    "Lusso" to Color(0xFFFBBF24),        // Oro
    "Inverno" to Color(0xFF22D3EE),      // Azzurro chiaro
    "Estate" to Color(0xFFFCD34D)        // Giallo chiaro
)

class CreaViaggioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EnterpriseMobileTheme {
                val factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val apiService = RetrofitClient.ottieniViaggioService(this@CreaViaggioActivity)
                        val dao = AppDatabase.getInstance(this@CreaViaggioActivity).viaggioDao()
                        val repository = ViaggioRepository(apiService, dao)
                        return CreaViaggioViewModel(
                            application = this@CreaViaggioActivity.application,
                            repository = repository
                        ) as T                } }

                val viewModel = ViewModelProvider(this@CreaViaggioActivity, factory)[CreaViaggioViewModel::class.java]

                // Recupera il nome utente dal SessionManager
                val sessionManager = remember { SessionManager(this) }
                val nomeOrganizzatore = sessionManager.ottieniUsername() ?: "Organizzatore"

                CreaViaggioScreen(
                    viewModel = viewModel,
                    nomeUtente = nomeOrganizzatore
                )
            }
        }
    }
}

// --- FUNZIONI DI SUPPORTO ---

fun apriDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dataFormattata = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(dataFormattata)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun apriDateTimePicker(context: Context, onDateTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val dataOraFormattata = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02dT%02d:%02d:00",
                        year, month + 1, dayOfMonth, hourOfDay, minute
                    )
                    onDateTimeSelected(dataOraFormattata)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun formattaPerDisplay(dataBackend: String): String {
    if (dataBackend.isBlank()) return ""
    return try {
        if (dataBackend.contains("T")) {
            val parti = dataBackend.split("T")
            val dataParti = parti[0].split("-")
            val oraParti = parti[1].split(":")
            "${dataParti[2]}/${dataParti[1]}/${dataParti[0]} ${oraParti[0]}:${oraParti[1]}"
        } else {
            val dataParti = dataBackend.split("-")
            if (dataParti.size == 3) {
                "${dataParti[2]}/${dataParti[1]}/${dataParti[0]}"
            } else {
                dataBackend
            }
        }
    } catch (e: Exception) {
        dataBackend
    }
}

// --- COMPONENTI UI STILIZZATI (TEMA SCURO) ---

@Composable
fun EnterpriseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.LightGray) },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkNavy.copy(alpha = 0.5f),
            unfocusedContainerColor = DarkNavy.copy(alpha = 0.5f),
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = AccentBlue,
            unfocusedLabelColor = Color.LightGray,
            focusedTextColor = WhiteText,
            unfocusedTextColor = WhiteText
        )
    )
}

@Composable
fun CampoDataClickabile(
    valore: String,
    etichetta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val valoreDisplay = formattaPerDisplay(valore)

    Box(modifier = modifier) {
        EnterpriseTextField(
            value = valoreDisplay,
            onValueChange = {},
            label = etichetta,
            readOnly = true
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

// ===== COMPONENTE TAG CON EMOJI E COLORI (TEMA SCURO) =====
@Composable
fun TagChip(
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val emoji = emojiMap[tag] ?: ""
    val colore = colorMap[tag] ?: Color.Gray

    val backgroundColor = if (isSelected) {
        colore.copy(alpha = 0.85f) // Colore pieno ma leggermente trasparente per tema scuro
    } else {
        colore.copy(alpha = 0.15f) // Pastello scuro
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        WhiteText.copy(alpha = 0.85f)
    }

    val borderColor = if (isSelected) {
        BorderStroke(2.dp, colore)
    } else {
        BorderStroke(1.dp, colore.copy(alpha = 0.4f))
    }

    Surface(
        modifier = Modifier
            .width(100.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = borderColor,
        shadowElevation = if (isSelected) 6.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = tag,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

// --- SCHERMATA PRINCIPALE ---

@Composable
fun CreaViaggioScreen(
    viewModel: CreaViaggioViewModel,
    nomeUtente: String
) {
    val context = LocalContext.current

    val titolo by viewModel.titolo.collectAsState()
    val descrizione by viewModel.descrizione.collectAsState()
    val partenza by viewModel.partenza.collectAsState()
    val destinazione by viewModel.destinazione.collectAsState()
    val prezzo by viewModel.prezzo.collectAsState()
    val dataInizio by viewModel.dataInizio.collectAsState()
    val dataFine by viewModel.dataFine.collectAsState()
    val postiDisponibili by viewModel.postiDisponibili.collectAsState()

    val tappe by viewModel.tappe.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Stato per i tag
    val tagDisponibili by viewModel.tagDisponibili.collectAsState()
    val tagSelezionati by viewModel.tagSelezionati.collectAsState()
    val isLoadingTag by viewModel.isLoadingTag.collectAsState()

    // EnterpriseScaffold con titolo vuoto e nome utente dinamico
    EnterpriseScaffold(
        titolo = "",
        nomeUtente = nomeUtente,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? CreaViaggioActivity)?.finish() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nuovo Progetto di Viaggio",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WhiteText
            )

            EnterpriseTextField(value = titolo, onValueChange = { viewModel.titolo.value = it }, label = "Titolo Esperienza")
            EnterpriseTextField(value = descrizione, onValueChange = { viewModel.descrizione.value = it }, label = "Descrizione", modifier = Modifier.height(100.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnterpriseTextField(value = partenza, onValueChange = { viewModel.partenza.value = it }, label = "Partenza", modifier = Modifier.weight(1f))
                EnterpriseTextField(value = destinazione, onValueChange = { viewModel.destinazione.value = it }, label = "Destinazione", modifier = Modifier.weight(1f))
            }

            EnterpriseTextField(value = prezzo, onValueChange = { viewModel.prezzo.value = it }, label = "Prezzo (€)")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoDataClickabile(
                    valore = dataInizio,
                    etichetta = "Data Inizio",
                    onClick = { apriDatePicker(context) { viewModel.dataInizio.value = it } },
                    modifier = Modifier.weight(1f)
                )
                CampoDataClickabile(
                    valore = dataFine,
                    etichetta = "Data Fine",
                    onClick = { apriDatePicker(context) { viewModel.dataFine.value = it } },
                    modifier = Modifier.weight(1f)
                )
            }

            EnterpriseTextField(value = postiDisponibili, onValueChange = { viewModel.postiDisponibili.value = it }, label = "Posti Disponibili")

            // ===== SEZIONE SELEZIONE TAG CON EMOJI E COLORI =====
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "🏷️ TAG DEL VIAGGIO",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = AccentBlue
            )
            Text(
                "Scegli da 1 a 3 tag per descrivere il tuo viaggio",
                fontSize = 13.sp,
                color = WhiteText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingTag) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else if (tagDisponibili.isEmpty()) {
                Text("Nessun tag disponibile.", color = Color.Gray)
            } else {
                // Layout a griglia per i tag (3 per riga)
                val chunks = tagDisponibili.chunked(3)
                chunks.forEach { riga ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        riga.forEach { tag ->
                            val isSelected = tagSelezionati.contains(tag)
                            val canSelect = isSelected || tagSelezionati.size < 3

                            TagChip(
                                tag = tag,
                                isSelected = isSelected,
                                onClick = { viewModel.toggleTag(tag) },
                                enabled = canSelect
                            )
                        }
                        // Riempi gli spazi vuoti
                        repeat(3 - riga.size) {
                            Spacer(modifier = Modifier.width(100.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Contatore tag selezionati con feedback visivo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            tagSelezionati.isEmpty() -> DangerRed.copy(alpha = 0.15f)
                            tagSelezionati.size == 3 -> SuccessGreen.copy(alpha = 0.15f)
                            else -> CardOverlay
                        }
                    ),
                    border = if (tagSelezionati.isEmpty()) {
                        BorderStroke(1.dp, DangerRed)
                    } else if (tagSelezionati.size == 3) {
                        BorderStroke(1.dp, SuccessGreen)
                    } else {
                        null
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                tagSelezionati.isEmpty() -> "⚠️ "
                                tagSelezionati.size == 3 -> "✅ "
                                else -> "📌 "
                            },
                            fontSize = 14.sp
                        )
                        Text(
                            text = when {
                                tagSelezionati.isEmpty() -> "Seleziona almeno 1 tag (obbligatorio)"
                                tagSelezionati.size == 3 -> "3/3 tag selezionati (completato)"
                                else -> "${tagSelezionati.size}/3 tag selezionati"
                            },
                            fontSize = 13.sp,
                            color = when {
                                tagSelezionati.isEmpty() -> DangerRed
                                tagSelezionati.size == 3 -> SuccessGreen
                                else -> WhiteText.copy(alpha = 0.8f)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mostra i tag selezionati con emoji
                if (tagSelezionati.isNotEmpty()) {
                    Text(
                        text = "I tuoi tag:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = WhiteText.copy(alpha = 0.6f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagSelezionati.forEach { tag ->
                            val emoji = emojiMap[tag] ?: ""
                            val colore = colorMap[tag] ?: Color.Gray
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colore.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, colore)
                            ) {
                                Text(
                                    text = "$emoji $tag",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colore
                                )
                            }
                        }
                    }
                }
            }
            // ===== FINE SEZIONE TAG =====

            Spacer(modifier = Modifier.height(8.dp))

            Text("TAPPE DEL VIAGGIO", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AccentBlue)

            if (tappe.isEmpty()) {
                Text("Nessuna tappa aggiunta. Clicca il bottone qui sotto per iniziare!", color = Color.Gray)
            } else {
                tappe.forEachIndexed { index, tappa ->
                    TappaFormItem(
                        tappa = tappa,
                        numeroTappa = index + 1,
                        onUpdate = { aggiornamento -> viewModel.aggiornaTappa(tappa.id, aggiornamento) },
                        onRemove = { viewModel.rimuoviTappa(tappa.id) },
                        context = context
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.aggiungiTappa() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
            ) {
                Text("+ Aggiungi Nuova Tappa", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.salvaViaggio(context) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                enabled = uiState !is CreaViaggioState.Loading
            ) {
                if (uiState is CreaViaggioState.Loading) {
                    CircularProgressIndicator(color = WhiteText, modifier = Modifier.size(24.dp))
                } else {
                    Text("Salva Viaggio e Tappe", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WhiteText)
                }
            }

            if (uiState is CreaViaggioState.Error) {
                Text(text = (uiState as CreaViaggioState.Error).message, color = DangerRed)
            } else if (uiState is CreaViaggioState.Success) {
                Text(text = "Viaggio salvato con successo!", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TappaFormItem(
    tappa: TappaState,
    numeroTappa: Int,
    onUpdate: (TappaState) -> Unit,
    onRemove: () -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardOverlay),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📍 Tappa $numeroTappa", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = WhiteText)
                TextButton(onClick = onRemove) {
                    Text("❌ Rimuovi", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }

            EnterpriseTextField(value = tappa.titoloTappa, onValueChange = { onUpdate(tappa.copy(titoloTappa = it)) }, label = "Titolo Tappa")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnterpriseTextField(value = tappa.costo, onValueChange = { onUpdate(tappa.copy(costo = it)) }, label = "Costo (€)", modifier = Modifier.weight(1f))
                EnterpriseTextField(value = tappa.posizione, onValueChange = { onUpdate(tappa.copy(posizione = it)) }, label = "Posizione", modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoDataClickabile(
                    valore = tappa.orarioInizio,
                    etichetta = "Inizio",
                    onClick = { apriDateTimePicker(context) { onUpdate(tappa.copy(orarioInizio = it)) } },
                    modifier = Modifier.weight(1f)
                )
                CampoDataClickabile(
                    valore = tappa.orarioFine,
                    etichetta = "Fine",
                    onClick = { apriDateTimePicker(context) { onUpdate(tappa.copy(orarioFine = it)) } },
                    modifier = Modifier.weight(1f)
                )
            }

            EnterpriseTextField(value = tappa.descrizioneTappa, onValueChange = { onUpdate(tappa.copy(descrizioneTappa = it)) }, label = "Descrizione Tappa", modifier = Modifier.height(80.dp))
        }
    }
}