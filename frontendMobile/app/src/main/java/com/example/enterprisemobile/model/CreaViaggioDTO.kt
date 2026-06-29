package com.example.enterprisemobile.model

data class TappaDTO(
    val titolo: String,
    val costo: Double,
    val posizione: String,
    val orarioInizio: String,
    val orarioFine: String,
    val descrizione: String
)

data class CreaViaggioDTO(
    val titolo: String,
    val descrizione: String,
    val cittaPartenza: String,
    val destinazione: String,
    val prezzo: Double,
    val dataInizio: String,
    val dataFine: String,
    val maxPartecipanti: Int,
    val latitudine: Double = 0.0,
    val longitudine: Double = 0.0,
    val tappe: List<TappaDTO>
)