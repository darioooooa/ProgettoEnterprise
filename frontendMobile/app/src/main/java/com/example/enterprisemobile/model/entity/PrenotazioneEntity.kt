package com.example.enterprisemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prenotazioni")
data class PrenotazioneEntity(
    @PrimaryKey val id: Long,
    val dataPrenotazione: String?,
    val numeroPersone: Int,
    val viaggioId: Long,
    val viaggioTitolo: String,
    val viaggioDestinazione: String?,
    val viaggioDataInizio: String?,
    val viaggioDataFine: String?,
    val stato: String
)