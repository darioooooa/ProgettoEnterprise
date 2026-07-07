package com.example.enterprisemobile.model

data class SegnalazioneDTO(
    val id: Int,
    val tipo: String,
    val motivo: String,
    val descrizione: String,
    val segnalatoreUsername: String,
    val riferimentoNome: String,
    val stato: String
)