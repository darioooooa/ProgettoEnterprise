package com.example.enterprisemobile.model

data class AttivitaViaggioDTO(
    val id: Long? = null,
    val titolo: String,
    val descrizione: String?,
    val orarioInizio: String,
    val orarioFine: String,
    val posizione: String,
    val costo: Double
)