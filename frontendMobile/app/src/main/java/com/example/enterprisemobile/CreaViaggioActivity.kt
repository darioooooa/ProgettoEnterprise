package com.example.enterprisemobile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
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

                        return CreaViaggioViewModel(repository) as T
                    }
                }

                val viewModel = ViewModelProvider(this@CreaViaggioActivity, factory)[CreaViaggioViewModel::class.java]
                CreaViaggioScreen(viewModel = viewModel)
            }
        }
    }
}

fun apriDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Formatta automaticamente in yyyy-MM-dd per Spring Boot!
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
@Composable
fun CampoDataClickabile(
    valore: String,
    etichetta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val valoreDisplay = formattaPerDisplay(valore)

    Box(modifier = modifier) {
        OutlinedTextField(
            value = valoreDisplay,
            onValueChange = {},
            label = { Text(etichetta) },
            modifier = Modifier.fillMaxWidth(),
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

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nuovo Progetto di Viaggio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = titolo, onValueChange = { viewModel.titolo.value = it }, label = { Text("Titolo Esperienza") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = descrizione, onValueChange = { viewModel.descrizione.value = it }, label = { Text("Descrizione") }, modifier = Modifier.fillMaxWidth().height(100.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = partenza, onValueChange = { viewModel.partenza.value = it }, label = { Text("Partenza") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = destinazione, onValueChange = { viewModel.destinazione.value = it }, label = { Text("Destinazione") }, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(value = prezzo, onValueChange = { viewModel.prezzo.value = it }, label = { Text("Prezzo (€)") }, modifier = Modifier.fillMaxWidth())

        // CAMPI DATA AGGIORNATI COL CALENDARIO
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        OutlinedTextField(value = postiDisponibili, onValueChange = { viewModel.postiDisponibili.value = it }, label = { Text("Posti Disponibili") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Text("TAPPE DEL VIAGGIO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

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

        OutlinedButton(onClick = { viewModel.aggiungiTappa() }, modifier = Modifier.fillMaxWidth()) {
            Text("+ Aggiungi Nuova Tappa")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.salvaViaggio(context) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = uiState !is CreaViaggioState.Loading
        ) {
            if (uiState is CreaViaggioState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Salva Viaggio e Tappe")
            }
        }

        if (uiState is CreaViaggioState.Error) {
            Text(text = (uiState as CreaViaggioState.Error).message, color = MaterialTheme.colorScheme.error)
        } else if (uiState is CreaViaggioState.Success) {
            Text(text = "Viaggio salvato con successo!", color = Color(0xFF4CAF50))
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📍 Tappa $numeroTappa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onRemove) {
                    Text("❌ Rimuovi", color = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(value = tappa.titoloTappa, onValueChange = { onUpdate(tappa.copy(titoloTappa = it)) }, label = { Text("Titolo Tappa") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tappa.costo, onValueChange = { onUpdate(tappa.copy(costo = it)) }, label = { Text("Costo (€)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tappa.posizione, onValueChange = { onUpdate(tappa.copy(posizione = it)) }, label = { Text("Posizione") }, modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoDataClickabile(
                    valore = tappa.orarioInizio,
                    etichetta = "Inizio",
                    onClick = {
                        apriDateTimePicker(context) { onUpdate(tappa.copy(orarioInizio = it)) }
                    },
                    modifier = Modifier.weight(1f)
                )
                CampoDataClickabile(
                    valore = tappa.orarioFine,
                    etichetta = "Fine",
                    onClick = {
                        apriDateTimePicker(context) { onUpdate(tappa.copy(orarioFine = it)) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(value = tappa.descrizioneTappa, onValueChange = { onUpdate(tappa.copy(descrizioneTappa = it)) }, label = { Text("Descrizione Tappa") }, modifier = Modifier.fillMaxWidth().height(80.dp))
        }
    }
}