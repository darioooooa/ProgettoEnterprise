package com.example.enterprisemobile.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import com.example.enterprisemobile.viewmodels.ChatViewModel

@Composable
fun SchermataDellaChat(
    modelloDiVistaChat: ChatViewModel,
    identificativoDellaStanza: Long,
    nomeDelMittenteLocale: String
) {
    val listaDeiMessaggiAttuali by modelloDiVistaChat.messaggiVisibili.collectAsState()
    var testoDelNuovoMessaggio by remember { mutableStateOf("") }

    LaunchedEffect(identificativoDellaStanza) {
        modelloDiVistaChat.entraNellaStanza(identificativoDellaStanza)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaDeiMessaggiAttuali) { singoloMessaggio ->

                val eMioMessaggio = singoloMessaggio.nomeDelMittente == nomeDelMittenteLocale

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (eMioMessaggio) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (eMioMessaggio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!eMioMessaggio) {
                                Text(
                                    text = singoloMessaggio.nomeDelMittente,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = singoloMessaggio.contenutoDelMessaggio,
                                color = if (eMioMessaggio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = testoDelNuovoMessaggio,
                onValueChange = { nuovoTesto -> testoDelNuovoMessaggio = nuovoTesto },
                modifier = Modifier.weight(1f),
                label = { Text("Scrivi il tuo messaggio qui...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (testoDelNuovoMessaggio.isNotBlank()) {
                        modelloDiVistaChat.inviaIlTuoMessaggio(
                            identificativoStanza = identificativoDellaStanza,
                            nomeMittente = nomeDelMittenteLocale,
                            testoMessaggio = testoDelNuovoMessaggio
                        )
                        testoDelNuovoMessaggio = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Invia")
            }
        }
    }
}