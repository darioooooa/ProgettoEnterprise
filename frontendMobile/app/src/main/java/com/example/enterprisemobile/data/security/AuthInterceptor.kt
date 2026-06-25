package com.example.enterprisemobile.data.security

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val richiestaOriginale = chain.request()
        val builderNuovaRichiesta = richiestaOriginale.newBuilder()

        // Estrae il token crittografato dall'hardware del telefono
        val token = sessionManager.ottieniTokenAccesso()

        // Se l'utente è loggato ed esiste un token valido, inietta l'header HTTP Bearer
        if (token != null) {
            builderNuovaRichiesta.addHeader("Authorization", "Bearer $token")
        }

        // Fa proseguire la chiamata HTTP verso Spring Boot
        return chain.proceed(builderNuovaRichiesta.build())
    }
}