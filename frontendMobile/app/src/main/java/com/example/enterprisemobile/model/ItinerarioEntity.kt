package com.example.enterprisemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itinerari")
data class ItinerarioEntity(
    @PrimaryKey val idItinerario: Long,
    val nome: String,
    val inCondivisione: Boolean,
    val proprietarioUsername: String?,
    val dataCreazioneStr: String?, // Salvata come stringa formattata
    val visibilita: String,
    val viaggiContenutiJson: String, // Stringa JSON convertita con Gson
    val isCondivisoConMe: Boolean = false
)