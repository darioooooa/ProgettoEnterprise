package com.example.enterprisemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "viaggi")
data class ViaggioEntity(
    @PrimaryKey val id: Long,
    val titolo: String,
    val destinazione: String,
    val cittaPartenza: String,
    val prezzo: Double,
    val dataInizio: String,
    val dataFine: String
)