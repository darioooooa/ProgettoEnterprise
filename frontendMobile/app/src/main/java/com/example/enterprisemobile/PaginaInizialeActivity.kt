package com.example.enterprisemobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enterprisemobile.ui.theme.DarkNavy
import com.example.enterprisemobile.ui.theme.WhiteText

@Composable
fun SchermataLanding(onNavigaALogin: () -> Unit, onNavigaARegistrazione: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkNavy).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ENTERPRISE", color = WhiteText, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Scopri le meraviglie del mondo. Inizia la tua avventura oggi.", color = Color.LightGray, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onNavigaALogin,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Accedi", color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigaARegistrazione,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WhiteText),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
        ) {
            Text("Registrati", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}