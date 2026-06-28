package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.AmiciziaDTO
import retrofit2.http.*

interface AmiciziaApiService {
    @GET("amicizie/miei-amici")
    suspend fun getMieiAmici(): List<AmiciziaDTO>

    @GET("amicizie/richieste/ricevute")
    suspend fun getRichiesteRicevute(): List<AmiciziaDTO>

    @GET("amicizie/richieste/inviate")
    suspend fun getRichiesteInviate(): List<AmiciziaDTO>

    @POST("amicizie/richiesta/{username}")
    suspend fun inviaRichiesta(@Path("username") username: String): AmiciziaDTO

    @PATCH("amicizie/{id}/accetta")
    suspend fun accettaRichiesta(@Path("id") id: Long): AmiciziaDTO

    @PATCH("amicizie/{id}/rifiuta")
    suspend fun rifiutaRichiesta(@Path("id") id: Long)

    @DELETE("amicizie/rimuovi/{amicoId}")
    suspend fun rimuoviAmico(@Path("amicoId") amicoId: Long) // NOTA: Spring si aspetta l'ID dell'utente amico, non della relazione!
}