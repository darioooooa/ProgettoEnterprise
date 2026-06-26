package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.PageResponse
import com.example.enterprisemobile.model.ViaggioDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ViaggioApiService {
    @GET("viaggi") // "api/v1/" è già nel BASE_URL di RetrofitClient
    suspend fun getViaggi(): PageResponse<ViaggioDTO>

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
}