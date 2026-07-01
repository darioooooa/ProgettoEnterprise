package com.example.enterprisemobile

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.DiventaOrganizzatoreViewModel

class DiventaOrganizzatoreActivity : ComponentActivity() {
    private val viewModel: DiventaOrganizzatoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnterpriseMobileTheme {
                DiventaOrganizzatoreContent(viewModel)
            }
        }
    }
}

@Composable
fun DiventaOrganizzatoreContent(viewModel: DiventaOrganizzatoreViewModel) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val mioUsername = sessionManager.ottieniUsername() ?: "Utente"

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val nomeFile = ottieniNomeFileDaUri(context, it)
            // Salviamo sia il nome (per mostrarlo a schermo) sia l'Uri (per inviarlo)
            viewModel.nomeFileSelezionato = nomeFile ?: "Documento_Allegato"
            viewModel.uriFileSelezionato = it
        }
    }

    LaunchedEffect(viewModel.richiestaInviataConSuccesso) {
        if (viewModel.richiestaInviataConSuccesso) {
            Toast.makeText(context, "Candidatura inviata con successo!", Toast.LENGTH_LONG).show()
            (context as? Activity)?.finish()
        }
    }

    // Finestra di avviso per richiesta già in sospeso
    if (viewModel.mostraModaleInSospeso) {
        AlertDialog(
            onDismissRequest = { viewModel.chiudiModale() },
            title = { Text("Richiesta in sospeso", fontWeight = FontWeight.Bold) },
            text = { Text("Hai già inviato una richiesta in precedenza. Attendi che l'amministrazione valuti la tua candidatura.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.chiudiModale(); (context as? Activity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Ho capito", color = WhiteText, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }


    if (viewModel.mostraModaleConferma) {
        AlertDialog(
            onDismissRequest = { viewModel.mostraModaleConferma = false },
            title = { Text("Conferma invio", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler inviare la candidatura? Sarà inviata all'amministrazione per la valutazione.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.mostraModaleConferma = false
                        viewModel.inviaRichiesta(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Conferma Invio", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.mostraModaleConferma = false }) {
                    Text("Annulla", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }

    EnterpriseScaffold(
        titolo = "CANDIDATURA",
        nomeUtente = mioUsername,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nuova Candidatura", color = WhiteText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Inserisci i dettagli per diventare Organizzatore", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.messaggioErrore != null) {
                Surface(color = DangerRed.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(viewModel.messaggioErrore!!, color = DangerRed, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            CampoTestoScuro(etichetta = "USERNAME DESIDERATO", valore = viewModel.usernameRichiesto) { viewModel.usernameRichiesto = it }
            CampoTestoScuro(etichetta = "EMAIL PROFESSIONALE", valore = viewModel.emailProfessionale) { viewModel.emailProfessionale = it }
            CampoTestoScuro(etichetta = "PERCHÉ VUOI DIVENTARE ORGANIZZATORE?", valore = viewModel.motivazione, righe = 3) { viewModel.motivazione = it }
            CampoTestoScuro(etichetta = "BIOGRAFIA PROFESSIONALE", valore = viewModel.biografiaProfessionale, righe = 4) { viewModel.biografiaProfessionale = it }

            // Sezione per allegare il documento
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text("DOCUMENTO (CV, PORTFOLIO)", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = "Allega", tint = DarkNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scegli File", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    val coloreFile = if (viewModel.uriFileSelezionato != null) WhiteText else Color.Gray
                    Text(
                        text = viewModel.nomeFileSelezionato,
                        color = coloreFile,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsante principale che ora apre la finestra di conferma
            Button(
                onClick = { viewModel.mostraModaleConferma = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = DarkNavy, modifier = Modifier.size(24.dp))
                } else {
                    Text("INVIA CANDIDATURA", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CampoTestoScuro(etichetta: String, valore: String, righe: Int = 1, onValoreCambiato: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(etichetta, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valore,
            onValueChange = onValoreCambiato,
            minLines = righe,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WhiteText,
                unfocusedTextColor = WhiteText,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Color.Gray,
                cursorColor = AccentBlue
            )
        )
    }
}

fun ottieniNomeFileDaUri(context: Context, uri: Uri): String? {
    var nomeFile: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    nomeFile = it.getString(index)
                }
            }
        }
    }
    if (nomeFile == null) {
        nomeFile = uri.path?.let { java.io.File(it).name }
    }
    return nomeFile
}