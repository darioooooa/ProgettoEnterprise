package com.example.enterprisemobile.data.model

data class RichiestaPromozioneDTO(
    val id: Long? = null,
    val viaggiatoreId: Long? = null,
    val usernameRichiesto: String,
    val emailProfessionale: String,
    val motivazione: String,
    val biografiaProfessionale: String,
    val documentiLink: String,
    val stato: String? = null,
    val dataRichiesta: String? = null
)