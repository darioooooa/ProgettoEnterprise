package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.PrenotazioneDTO
import com.example.enterprisemobile.model.PageResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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
        @Query("page") page: Int = 0,
        @Query("stato") stato: String? = null,
        @Query("username") username: String? = null
    ): Response<PageResponse<PrenotazioneDTO>>

    @Streaming
    @GET("prenotazioni/{prenotazioneId}/esporta-calendario")
    suspend fun scaricaFileIcs(
        @Path("prenotazioneId") prenotazioneId: Long
    ): Response<ResponseBody>


}