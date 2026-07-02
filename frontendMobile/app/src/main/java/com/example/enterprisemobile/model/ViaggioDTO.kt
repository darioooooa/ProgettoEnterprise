package com.example.enterprisemobile.model

data class ViaggioDTO(
    val id: Long? = null,
    val titolo: String,
    val descrizione: String?,
    val stato: String,
    val maxPartecipanti: Int,
    val partecipantiAttuali: Int = 0,
    val destinazione: String,
    val cittaPartenza: String,
    val prezzo: Double,
    val dataInizio: String,
    val dataFine: String,
    val latitudine: Double,
    val longitudine: Double,
    val mediaRecensioni: Double = 0.0,
    val tappe: List<AttivitaViaggioDTO> = emptyList()
)