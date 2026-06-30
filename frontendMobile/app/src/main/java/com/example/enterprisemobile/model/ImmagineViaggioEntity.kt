package com.example.enterprisemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "immagini_viaggio")
data class ImmagineViaggioEntity(
    @PrimaryKey val id: Long,
    val viaggioId: Long,
    val url: String,
    val pubblica: Boolean
)