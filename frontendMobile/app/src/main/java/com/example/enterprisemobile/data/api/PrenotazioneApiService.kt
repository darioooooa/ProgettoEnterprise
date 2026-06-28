package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.data.model.PrenotazioneDTO
import com.example.enterprisemobile.model.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
data class PrenotazioneRequest(val numeroPersone: Int)

data class PrenotazioneResponse(val id: Long, val stato: String)

interface PrenotazioneApiService {
    @POST("prenotazioni/viaggi/{viaggioId}/prenota")
    suspend fun creaPrenotazione(
        @Path("viaggioId") viaggioId: Long,
        @Body richiesta: PrenotazioneRequest
    ): PrenotazioneResponse

    @GET("prenotazioni")
    suspend fun getMiePrenotazioni(
        @Query("page") page: Int = 0
    ): Response<PageResponse<PrenotazioneDTO>>
}