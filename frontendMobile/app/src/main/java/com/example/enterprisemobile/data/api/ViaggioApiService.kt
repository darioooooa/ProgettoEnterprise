package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.AttivitaViaggioDTO
import com.example.enterprisemobile.model.ImmagineViaggioDTO
import com.example.enterprisemobile.model.PageResponse
import com.example.enterprisemobile.model.RecensioneDTO
import com.example.enterprisemobile.model.StatisticheViaggioDTO
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.model.ViaggioMappaDTO
import com.example.enterprisemobile.model.CreaViaggioDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ViaggioApiService {
    @GET("viaggi") // "api/v1/" è già nel BASE_URL di RetrofitClient
    suspend fun getViaggi(): PageResponse<ViaggioDTO>

    @GET("viaggi/mappa-viaggi")
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


    @GET("viaggi/{id}")
    suspend fun getViaggioById(@Path("id") id: Long): Response<ViaggioDTO>

    @PUT("viaggi/{id}")
    suspend fun modificaViaggio(
        @Path("id") id: Long,
        @Body viaggioDTO: Map<String, String>
    ): Response<ResponseBody>

    @DELETE("viaggi/{id}")
    suspend fun eliminaViaggio(
        @Path("id") id: Long
    ): Response<ResponseBody>


    // Attività / programma
    @GET("viaggi/{id}/attivita-viaggi")
    suspend fun getAttivitaViaggio(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @QueryMap filtri: Map<String, String>
    ): Response<PageResponse<AttivitaViaggioDTO>>

    @POST("viaggi/{id}/attivita-viaggi")
    suspend fun creaAttivita(
        @Path("id") id: Long,
        @Body attivita: Map<String, String>
    ): Response<ResponseBody>

    @PUT("viaggi/{id}/attivita-viaggi/{attivitaId}/modifica")
    suspend fun modificaAttivita(
        @Path("id") id: Long,
        @Path("attivitaId") attivitaId: Long,
        @Body attivita: Map<String, String>
    ): Response<ResponseBody>

    @DELETE("viaggi/{id}/attivita-viaggi/{attivitaId}")
    suspend fun eliminaAttivita(
        @Path("id") id: Long,
        @Path("attivitaId") attivitaId: Long
    ): Response<ResponseBody>


    // Recensioni / community
    @GET("viaggi/{id}/recensioni")
    suspend fun getRecensioni(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @QueryMap filtri: Map<String, String>
    ): Response<PageResponse<RecensioneDTO>>

    @POST("viaggi/{id}/recensioni")
    suspend fun inviaRecensione(
        @Path("id") id: Long,
        @Body recensione: Map<String, String>
    ): Response<ResponseBody>

    @GET("viaggi/{id}/statistiche")
    suspend fun getStatisticheRecensioni(@Path("id") id: Long): Response<StatisticheViaggioDTO>

    @PUT("viaggi/{id}/recensioni/{recensioneId}")
    suspend fun aggiornaRecensione(
        @Path("id") id: Long,
        @Path("recensioneId") recensioneId: Long,
        @Body recensione: Map<String, String>
    ): Response<ResponseBody>

    @DELETE("viaggi/{id}/recensioni/{recensioneId}")
    suspend fun eliminaRecensione(
        @Path("id") id: Long,
        @Path("recensioneId") recensioneId: Long
    ): Response<ResponseBody>


    // Galleria viaggio
    @GET("viaggi/{id}/immagini")
    suspend fun getGalleria(@Path("id") id: Long): Response<List<ImmagineViaggioDTO>>

    @POST("viaggi/{id}/immagini")
    suspend fun aggiungiImmagine(
        @Path("id") id: Long,
        @Query("url") url: String,
        @Query("pubblica") pubblica: Boolean
    ): Response<ResponseBody>

    @DELETE("viaggi/{id}/immagini/{immagineId}")
    suspend fun eliminaImmagine(
        @Path("id") id: Long,
        @Path("immagineId") immagineId: Long
    ): Response<ResponseBody>

    @PATCH("viaggi/{id}/immagini/{immagineId}/visibilita")
    suspend fun modificaVisibilita(
        @Path("id") id: Long,
        @Path("immagineId") immagineId: Long,
        @Query("pubblica") pubblica: Boolean
    ): Response<ResponseBody>
}