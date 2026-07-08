package com.example.enterprisemobile.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.model.UtenteBannatoDTO
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.AdminViewModel

@Composable
fun GestioneBanScreen(viewModel: AdminViewModel) {
    val context = LocalContext.current
    var mostraModaleSbanna by remember { mutableStateOf(false) }
    var utenteDaSbannare by remember { mutableStateOf<Long?>(null) }

    val utentiBannati by viewModel.utentiBannati.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Utenti Bannati", color = WhiteText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (utentiBannati.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nessun utente bannato.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = utentiBannati,
                    key = { utente -> utente.id }
                ) { utente ->
                    Surface(
                        color = CardOverlay,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("👤 ${utente.username}", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Data: ${utente.dataBan ?: "N/D"}", color = Color.LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Motivo: ${utente.motivoBan ?: "N/D"}", color = DangerRed, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    utenteDaSbannare = utente.id.toLong()
                                    mostraModaleSbanna = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Text("🔓 Sbanna", color = WhiteText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostraModaleSbanna) {
        AlertDialog(
            onDismissRequest = { mostraModaleSbanna = false },
            title = { Text("Riattiva Utente", color = WhiteText, fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler riattivare questo utente? Potrà nuovamente accedere alla piattaforma.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        utenteDaSbannare?.let { id ->
                            viewModel.sbannaUtente(id,
                                onSuccess = {
                                    Toast.makeText(context, "Utente riattivato con successo!", Toast.LENGTH_SHORT).show()
                                    mostraModaleSbanna = false
                                    viewModel.caricaUtentiBannati()
                                },
                                onError = { err: String -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { mostraModaleSbanna = false }) { Text("Annulla", color = Color.Gray) } },
            containerColor = DarkNavy
        )
    }
}