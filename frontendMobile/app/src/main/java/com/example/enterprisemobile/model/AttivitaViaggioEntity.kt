package com.example.enterprisemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attivita_viaggio")
data class AttivitaViaggioEntity(
    @PrimaryKey val id: Long,
    val viaggioId: Long,
    val titolo: String,
    val descrizione: String?,
    val orarioInizio: String,
    val orarioFine: String,
    val posizione: String,
    val costo: Double
)