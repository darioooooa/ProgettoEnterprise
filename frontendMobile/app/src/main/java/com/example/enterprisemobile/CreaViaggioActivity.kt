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
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.viewmodels.CreaViaggioState
import com.example.enterprisemobile.viewmodels.CreaViaggioViewModel
import com.example.enterprisemobile.viewmodels.TappaState
import java.util.Calendar
import java.util.Locale

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

val colorMap = mapOf(
    "Mare" to Color(0xFF3B82F6),
    "Montagna" to Color(0xFF059669),
    "Città d'arte" to Color(0xFF8B5CF6),
    "Relax" to Color(0xFFEC4899),
    "Avventura" to Color(0xFFF97316),
    "Cultura" to Color(0xFF6366F1),
    "Enogastronomia" to Color(0xFFDC2626),
    "Economico" to Color(0xFF10B981),
    "Lusso" to Color(0xFFF59E0B),
    "Inverno" to Color(0xFF06B6D4),
    "Estate" to Color(0xFFFBBF24)
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
                        ) as T
                    }
                }
                val viewModel = ViewModelProvider(this@CreaViaggioActivity, factory)[CreaViaggioViewModel::class.java]
                CreaViaggioScreen(viewModel = viewModel)
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
    if (dataBackend.isBlank()) return " "
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

// --- COMPONENTI UI STILIZZATI ---
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
        label = { Text(text = label, fontWeight = FontWeight.SemiBold) },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF3F4F6),
            unfocusedContainerColor = Color(0xFFF3F4F6),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.DarkGray
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
        colore
    } else {
        colore.copy(alpha = 0.15f)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        Color(0xFF374151)
    }

    val borderColor = if (isSelected) {
        null
    } else {
        BorderStroke(1.5.dp, colore.copy(alpha = 0.4f))
    }

    Surface(
        modifier = Modifier
            .width(100.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = borderColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp,
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

// --- SCHERMATE PRINCIPALI ---
@Composable
fun CreaViaggioScreen(viewModel: CreaViaggioViewModel) {
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
    val tagDisponibili by viewModel.tagDisponibili.collectAsState()
    val tagSelezionati by viewModel.tagSelezionati.collectAsState()
    val isLoadingTag by viewModel.isLoadingTag.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuovo Progetto di Viaggio",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111827)
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

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "🏷️ TAG DEL VIAGGIO",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color(0xFF111827)
        )
        Text(
            "Scegli fino a 3 tag per descrivere il tuo viaggio",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingTag) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (tagDisponibili.isEmpty()) {
            Text("Nessun tag disponibile.", color = Color.Gray)
        } else {
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
                    repeat(3 - riga.size) {
                        Spacer(modifier = Modifier.width(100.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (tagSelezionati.size == 3) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFF3F4F6)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tagSelezionati.size == 3) "✅ " else "📌 ",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${tagSelezionati.size}/3 tag selezionati",
                        fontSize = 13.sp,
                        color = if (tagSelezionati.size == 3) Color(0xFF10B981) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("TAPPE DEL VIAGGIO", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF111827))

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
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("+ Aggiungi Nuova Tappa", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.salvaViaggio(context) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            enabled = uiState !is CreaViaggioState.Loading
        ) {
            if (uiState is CreaViaggioState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Salva Viaggio e Tappe", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (uiState is CreaViaggioState.Error) {
            Text(text = (uiState as CreaViaggioState.Error).message, color = MaterialTheme.colorScheme.error)
        } else if (uiState is CreaViaggioState.Success) {
            Text(text = "✅ Viaggio salvato con successo!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
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
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📍 Tappa $numeroTappa", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF111827))
                TextButton(onClick = onRemove) {
                    Text("❌ Rimuovi", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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