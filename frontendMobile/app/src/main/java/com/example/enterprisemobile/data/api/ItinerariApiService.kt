package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
interface ItinerariApiService {
    @GET("itinerari-preferiti/mie-liste")
    suspend fun ottieniMieListe(): List<ItinerarioPreferitoDTO>

    @GET("itinerari-preferiti/utente/{username}/pubblici")
    suspend fun getItinerariPubbliciAmico(
        @Path("username") username: String
    ): List<ItinerarioPreferitoDTO>

    @POST("itinerari-preferiti")
    suspend fun creaNuovoItinerario(
        @Body nuovoItinerario: ItinerarioPreferitoDTO
    ): ItinerarioPreferitoDTO

    @DELETE("itinerari-preferiti/{id}")
    suspend fun cancellaItinerarioDalServer(@Path("id") identificativoItinerario: Long)

}