package com.example.enterprisemobile.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.data.service.ServizioChat

class GeneratoreChatViewModel(private val contesto: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(classeModello: Class<T>): T {
        val gestoreSessione = SessionManager(contesto)
        val tokenSalvato = gestoreSessione.ottieniTokenAccesso() ?: ""

        val servizioDiChat = ServizioChat(tokenSalvato)
        val chiamateApiChat = RetrofitClient.ottieniChatService(contesto)

        @Suppress("UNCHECKED_CAST")
        return ChatViewModel(servizioDiChat, chiamateApiChat) as T
    }
}