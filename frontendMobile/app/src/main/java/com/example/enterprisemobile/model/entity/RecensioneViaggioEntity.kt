package com.example.enterprisemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recensioni_viaggio")
data class RecensioneViaggioEntity(
    @PrimaryKey val id: Long,
    val viaggioId: Long,
    val voto: Int,
    val commento: String?,
    val utenteUsername: String?,
    val dataCreazione: String?
)