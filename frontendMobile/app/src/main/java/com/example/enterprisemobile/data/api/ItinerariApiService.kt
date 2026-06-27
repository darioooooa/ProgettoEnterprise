package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import retrofit2.http.GET

interface ItinerariApiService {
    @GET("itinerari-preferiti/mie-liste")
    suspend fun ottieniMieListe(): List<ItinerarioPreferitoDTO>
}