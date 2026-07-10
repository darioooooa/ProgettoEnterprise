package com.example.enterprisemobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "richieste_promozione")
data class RichiestaPromozioneEntity(
    @PrimaryKey val id: Long,
    val usernameViaggiatore: String,
    val emailViaggiatore: String,
    val dataRichiesta: String,
    val motivazione: String,
    val stato: String,
    val biografiaProfessionale: String,
    val documentiLink: String?,
    val adminId: Long?,
    val usernameRichiesto: String,
    val emailProfessionale: String,
    val timestampAggiornamento: Long = System.currentTimeMillis()
)