package com.example.enterprisemobile.data.security

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val richiestaOriginale = chain.request()
        val token = sessionManager.ottieniTokenAccesso()

        // DEBUG: Stampa tutto!
        android.util.Log.d("DEBUG_AUTH", "Token grezzo recuperato: $token")

        val builder = richiestaOriginale.newBuilder()

        if (token != null) {
            // Se il token contiene già "Bearer ", non aggiungerlo di nuovo
            val headerValue = if (token.startsWith("Bearer ")) token else "Bearer $token"

            android.util.Log.d("DEBUG_AUTH", "Header Authorization inviato: $headerValue")
            builder.addHeader("Authorization", headerValue)
        }

        return chain.proceed(builder.build())
    }
}