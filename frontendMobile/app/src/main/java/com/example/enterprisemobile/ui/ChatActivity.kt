package com.example.enterprisemobile

import ChatViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.ui.components.SchermataDellaChat
import com.example.enterprisemobile.ui.theme.EnterpriseMobileTheme
import com.example.enterprisemobile.viewmodels.ChatViewModel

class ChatActivity : ComponentActivity() {

    private val modelloDiVistaDellaChat: ChatViewModel by viewModels {
        ChatViewModelFactory(application)}
    override fun onCreate(statoSalvato: Bundle?) {
        super.onCreate(statoSalvato)

        val identificativoDellaStanza = intent.getLongExtra("ID_STANZA", -1L)

        val gestoreDiSessione = SessionManager(this)
        val nomeUtenteReale = gestoreDiSessione.ottieniUsername() ?: "UtenteSconosciuto"
        val ruoloUtente = gestoreDiSessione.ottieniRuolo() ?: ""
        val utenteEOrganizzatore = ruoloUtente == "ROLE_ORGANIZZATORE"
        val identificativoUtente = gestoreDiSessione.ottieniIdUtente()?.toLongOrNull() ?: 0L
        setContent {
            EnterpriseMobileTheme {
                SchermataDellaChat(
                    modelloDiVistaChat = modelloDiVistaDellaChat,
                    identificativoDellaStanza = identificativoDellaStanza,
                    nomeDelMittenteLocale = nomeUtenteReale,
                    seiOrganizzatore = utenteEOrganizzatore,
                    identificativoUtenteLocale = identificativoUtente,
                    onIndietroPremuto = { finish() }
                )
            }
        }
    }
}