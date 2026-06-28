package com.example.enterprisemobile.data.model

data class AmiciziaDTO(
    val id: Long,
    val richiedenteId: Long,
    val richiedenteUsername: String,
    val riceventeId: Long,
    val riceventeUsername: String,
    val stato: String,
    val dataRichiesta: String? = null,
    val dataRisposta: String? = null
)