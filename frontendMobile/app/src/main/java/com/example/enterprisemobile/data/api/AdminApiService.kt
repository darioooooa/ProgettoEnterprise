package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.RichiestaPromozioneResponse
import com.example.enterprisemobile.model.PageResponse
import com.example.enterprisemobile.model.SegnalazioneDTO
import com.example.enterprisemobile.model.UtenteBannatoDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

data class CreaSegnalazioneRequest(
    val tipo: String,
    val idRiferimento: Long,
    val motivo: String,
    val descrizione: String
)
interface AdminApiService {
    @GET("admin/richieste")
    suspend fun getRichiestePromozione(
        @Query("stato") stato: String? = null,
        @Query("username") username: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<RichiestaPromozioneResponse>>

    @POST("admin/richieste/{id}/approva")
    suspend fun approvaRichiesta(@Path("id") id: Long): Response<Unit>

    @POST("admin/richieste/{id}/rifiuta")
    suspend fun rifiutaRichiesta(
        @Path("id") id: Long,
        @Body note: Map<String, String>
    ): Response<Unit>

    @GET("admin/richieste/promozioni/{id}/documento")
    suspend fun scaricaDocumento(@Path("id") id: Long): Response<ResponseBody>

    @GET("segnalazioni/ricerca")
    suspend fun getSegnalazioni(
        @Query("tipo") tipo: String? = null,
        @Query("stato") stato: List<String>? = null,
        @Query("usernameSegnalatore") usernameSegnalatore: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<SegnalazioneDTO>>

    @PUT("segnalazioni/{id}/prendi-in-carico")
    suspend fun prendiInCarico(@Path("id") id: Long, @Query("idAdmin") idAdmin: Long): Response<SegnalazioneDTO>

    @PUT("segnalazioni/{id}/risolvi")
    suspend fun risolviSegnalazione(
        @Path("id") id: Long,
        @Query("idAdmin") idAdmin: Long,
        @Query("sospendiAutore") sospendiAutore: Boolean
    ): Response<SegnalazioneDTO>

    @PUT("segnalazioni/{id}/rifiuta")
    suspend fun rifiutaSegnalazione(@Path("id") id: Long, @Query("idAdmin") idAdmin: Long): Response<SegnalazioneDTO>

    @POST("segnalazioni/{id}/invia-avvertimento")
    suspend fun inviaAvvertimentoUtente(
        @Path("id") id: Long,
        @Query("idAdmin") idAdmin: Long
    ): Response<Unit>

    @POST("segnalazioni/crea")
    suspend fun creaSegnalazione(
        @Body segnalazione: CreaSegnalazioneRequest,
        @Query("idSegnalatore") idSegnalatore: Long
    ): Response<Unit>
    @GET("admin/richieste/utenti-bannati")
    suspend fun getUtentiBannati(): Response<List<UtenteBannatoDTO>>

    @PUT("admin/richieste/utenti/{id}/riattiva")
    suspend fun riattivaUtente(@Path("id") id: Long): Response<Void>
}