package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.RichiestaPromozioneResponse
import com.example.enterprisemobile.model.PageResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {

    @GET("admin/richieste")
    suspend fun getRichiestePromozione(
        @Query("stato") stato: String? = null,
        @Query("username") username: String? = null,  // ✅ NUOVO
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<RichiestaPromozioneResponse>>

    @POST("admin/richieste/{id}/approva")
    suspend fun approvaRichiesta(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("admin/richieste/{id}/rifiuta")
    suspend fun rifiutaRichiesta(
        @Path("id") id: Long,
        @Body richiesta: Map<String, String>
    ): Response<Unit>

    @GET("/api/v1/admin/richieste/promozioni/{id}/documento")
    suspend fun scaricaDocumento(@Path("id") id: Long): Response<ResponseBody>
}