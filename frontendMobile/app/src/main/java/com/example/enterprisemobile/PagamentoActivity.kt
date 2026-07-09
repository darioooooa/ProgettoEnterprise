package com.example.enterprisemobile

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.components.EnterpriseScaffold
import com.example.enterprisemobile.ui.theme.*
import com.example.enterprisemobile.viewmodels.PagamentoViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet

class PagamentoActivity : ComponentActivity() {
    private val viewModel: PagamentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PaymentConfiguration.init(this, "pk_test_51TfqDSR7OooPKO1apYDbncshdjPXl78dVRdSImSkL1V54h5RZ9EyOjs8Pa2PFimRyd0GT7qheedIfNayWRcWV86Q00XZLe0gsB")

        val idPrenotazione = intent.getLongExtra("ID_PRENOTAZIONE", -1L)
        val importoDaPagare = intent.getDoubleExtra("IMPORTO", 0.0)

        setContent {
            EnterpriseMobileTheme {
                PagamentoContent(viewModel, idPrenotazione, importoDaPagare)
            }
        }
    }
}

@Composable
fun PagamentoContent(viewModel: PagamentoViewModel, idPrenotazione: Long, importo: Double) {
    val context = LocalContext.current

    // Inizializza il componente UI di Stripe
    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.gestisciRisultatoStripe(result, idPrenotazione, importo)
    }

    // Appena si apre la pagina, chiediamo a Spring Boot il permesso di pagare
    LaunchedEffect(Unit) {
        if (idPrenotazione != -1L) {
            viewModel.preparaPagamento(idPrenotazione)
        }
    }

    // Osserviamo quando il pagamento finisce con successo
    LaunchedEffect(viewModel.pagamentoCompletato) {
        if (viewModel.pagamentoCompletato) {
            Toast.makeText(context, "🎉 Pagamento confermato! Prenotazione valida.", Toast.LENGTH_LONG).show()
            (context as? Activity)?.finish()
        }
    }

    EnterpriseScaffold(
        titolo = "PAGAMENTO",
        nomeUtente = viewModel.nomeUtente,
        mostraFrecciaIndietro = true,
        onBackClick = { (context as? Activity)?.finish() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 1f))
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Conferma Prenotazione", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Totale da pagare: €$importo", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = viewModel.nomeTitolare,
                        onValueChange = { viewModel.nomeTitolare = it },
                        label = { Text("Titolare della carta") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (viewModel.messaggioErrore != null) {
                        Text("⚠️ ${viewModel.messaggioErrore}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.clientSecret?.let { secret ->
                                paymentSheet.presentWithPaymentIntent(
                                    secret,
                                    PaymentSheet.Configuration(merchantDisplayName = "Enterprise Viaggi")
                                )
                            }
                        },
                        // Il bottone si abilita solo quando il server ha risposto (clientSecret != null) e si ha inserito il nome
                        enabled = !viewModel.isCaricamento && viewModel.clientSecret != null && viewModel.nomeTitolare.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        if (viewModel.isCaricamento) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("PAGA ORA 💳", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}