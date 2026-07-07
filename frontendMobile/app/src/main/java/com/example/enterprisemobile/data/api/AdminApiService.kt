package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.RichiestaPromozioneResponse
import com.example.enterprisemobile.model.PageResponse
import com.example.enterprisemobile.model.SegnalazioneDTO
import com.example.enterprisemobile.model.UtenteBannatoDTO
import com.example.enterprisemobile.model.SegnalazioneFiltroDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {

    // --- RICHIESTE PROMOZIONE ---
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

    // --- SEGNALAZIONI (Allineato al SegnalazioneController) ---

    // NOTA: Il tuo SegnalazioneController usa @PostMapping("/cerca")
    @GET("segnalazioni/ricerca")
        suspend fun getSegnalazioni(
        @Query("tipo") tipo: String? = null,
        @Query("stato") stato: String? = null,
        @Query("pagina") pagina: Int = 0,
        @Query("dimensione") dimensione: Int = 50
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

    // --- UTENTI BANNATI (Allineato ad AdminController) ---

    @GET("admin/richieste/utenti-bannati")
    suspend fun getUtentiBannati(): Response<List<UtenteBannatoDTO>>

    @PUT("admin/richieste/utenti/{id}/riattiva")
    suspend fun riattivaUtente(@Path("id") id: Long): Response<Void>
}