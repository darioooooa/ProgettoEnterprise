package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.RispostaToken
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService {

    // Chiama l'endpoint OpenID Connect standard esposto dal Keycloak Realm
    @FormUrlEncoded
    @POST("realms/enterprise-realm/protocol/openid-connect/token")
    suspend fun effettuaAccesso(
        @FieldMap parametri: Map<String, String>
    ): Response<RispostaToken>
}