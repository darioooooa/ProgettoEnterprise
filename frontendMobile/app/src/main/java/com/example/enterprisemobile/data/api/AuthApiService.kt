package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.RispostaToken
import com.example.enterprisemobile.model.UtenteDTO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService {

    // Utilizzato nel ViewModel in modo asincrono per il Login standard
    @FormUrlEncoded
    @POST("realms/enterprise-realm/protocol/openid-connect/token")
    suspend fun effettuaAccesso(
        @FieldMap parametri: Map<String, String>
    ): Response<RispostaToken>

    // Utilizzato nell'Interceptor in modo sincrono per il refresh automatico
    @FormUrlEncoded
    @POST("realms/enterprise-realm/protocol/openid-connect/token")
    fun effettuaAccessoSync(
        @FieldMap parametri: Map<String, String>
    ): Call<RispostaToken>

}