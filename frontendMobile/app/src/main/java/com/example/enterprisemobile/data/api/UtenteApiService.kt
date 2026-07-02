package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.UtenteDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UtenteApiService {
    @GET("utenti/me")
    suspend fun ottieniMioProfilo(): Response<UtenteDTO>

    // Recupera il profilo pubblico di un qualsiasi utente tramite id
    @GET("utenti/{id}")
    suspend fun ottieniProfiloPubblico(
        @Path("id") id: Long
    ): Response<UtenteDTO>

    // Recupero password
    @POST("utenti/recupero-password")
    suspend fun recuperaPassword(
        @Query("email") email: String
    ): Response<ResponseBody>

    @POST("utenti/aggiorna-token")
    suspend fun aggiornaToken(@Body payload: Map<String, String>): retrofit2.Response<Unit>
}