package com.example.enterprisemobile.data.security

import android.content.Context
import android.content.Intent
import com.example.enterprisemobile.MainActivity
import com.example.enterprisemobile.data.api.AuthApiService
import com.example.enterprisemobile.data.api.RetrofitClient
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val richiestaOriginale = chain.request()
        val token = sessionManager.ottieniTokenAccesso()

        val builder = richiestaOriginale.newBuilder()

        if (token != null) {
            val headerValue = if (token.startsWith("Bearer ")) token else "Bearer $token"
            builder.addHeader("Authorization", headerValue)
        }

        // Richiesta verso Spring Boot
        var risposta = chain.proceed(builder.build())

        // Se si riceve 401 Unauthorized, il token di accesso potrebbe essere scaduto
        if (risposta.code() == 401 && token != null) {
            // Si chiude la risposta fallita per non lasciare socket appesi
            risposta.close()

            val refreshToken = sessionManager.ottieniRefreshToken()
            if (refreshToken != null) {
                // Sincronizzazione per evitare refresh multipli in caso di chiamate concorrenti
                synchronized(this) {
                    val parametriRefresh = mapOf(
                        "grant_type" to "refresh_token",
                        "client_id" to "enterprise-client",
                        "refresh_token" to refreshToken
                    )

                    val authService = RetrofitClient.ottieniClientKeycloak(context).create(AuthApiService::class.java)
                    val esecuzioneRefresh = authService.effettuaAccessoSync(parametriRefresh).execute()

                    if (esecuzioneRefresh.isSuccessful && esecuzioneRefresh.body() != null) {
                        val nuoviToken = esecuzioneRefresh.body()!!

                        // Salvataggio della sessione rinnovata
                        sessionManager.salvaTokenAccesso(nuoviToken.accessToken)
                        nuoviToken.refreshToken?.let { sessionManager.salvaRefreshToken(it) }

                        // Costruzione della nuova richiesta con il token appena rigenerato
                        val nuovaRichiesta = richiestaOriginale.newBuilder()
                            .header("Authorization", "Bearer ${nuoviToken.accessToken}")
                            .build()

                        // Restituzione della risposta del secondo tentativo andato a buon fine
                        risposta = chain.proceed(nuovaRichiesta)
                    } else {
                        // Il refresh token è scaduto o non è più valido: si forza il logout totale
                        sessionManager.cancellaSessione()

                        val intentLogOut = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intentLogOut)
                    }
                }
            }
        }

        return risposta
    }
}