package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.PageResponse
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.model.ViaggioMappaDTO
import com.example.enterprisemobile.model.CreaViaggioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST

interface ViaggioApiService {
    @GET("viaggi") // "api/v1/" è già nel BASE_URL di RetrofitClient
    suspend fun getViaggi(): PageResponse<ViaggioDTO>

    @GET("api/v1/viaggi/mappa-viaggi")
    suspend fun getViaggiPerMappa(): List<ViaggioMappaDTO>

    // Metodo per filtrare i viaggi
    @GET("viaggi")
    suspend fun getViaggiFiltrati(
        @Query("destinazione") destinazione: String,
        @Query("dataMin") dataMin: String,
        @Query("dataMax") dataMax: String,
        @Query("maxPartecipanti") posti: String,
        @Query("prezzoMin") prezzoMin: String,
        @Query("prezzoMax") prezzoMax: String,
        @Query("page") page: Int
    ): PageResponse<ViaggioDTO>

    @POST("viaggi")
    suspend fun creaViaggio(@Body nuovoViaggio : CreaViaggioDTO): Response<ViaggioDTO>

}