package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface ItinerariApiService {
    @GET("itinerari-preferiti/mie-liste")
    suspend fun ottieniMieListe(): List<ItinerarioPreferitoDTO>

    @GET("itinerari-preferiti/utente/{username}/pubblici")
    suspend fun getItinerariPubbliciAmico(
        @Path("username") username: String
    ): List<ItinerarioPreferitoDTO>
}