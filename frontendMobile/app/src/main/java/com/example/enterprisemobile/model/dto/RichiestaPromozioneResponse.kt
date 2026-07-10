package com.example.enterprisemobile.data.model

data class RichiestaPromozioneResponse(
    val id: Long,
    val usernameViaggiatore: String,
    val emailViaggiatore: String,
    val dataRichiesta: String,
    val motivazione: String,
    val stato: String,
    val biografiaProfessionale: String,
    val documentiLink: String?,
    val adminId: Long?,
    val usernameRichiesto: String,
    val emailProfessionale: String
)