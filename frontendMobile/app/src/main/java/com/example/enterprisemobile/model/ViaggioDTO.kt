package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class ViaggioDTO(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("titolo") val titolo: String,
    @SerializedName("descrizione") val descrizione: String?,
    @SerializedName("stato") val stato: String,
    @SerializedName("maxPartecipanti") val maxPartecipanti: Int,
    @SerializedName("partecipantiAttuali") val partecipantiAttuali: Int = 0,
    @SerializedName("destinazione") val destinazione: String,
    @SerializedName("cittaPartenza") val cittaPartenza: String,
    @SerializedName("prezzo") val prezzo: Double,
    @SerializedName("dataInizio") val dataInizio: String,
    @SerializedName("dataFine") val dataFine: String,
    @SerializedName("latitudine") val latitudine: Double,
    @SerializedName("longitudine") val longitudine: Double,
    @SerializedName("mediaRecensioni") val mediaRecensioni: Double = 0.0,
    @SerializedName("numeroRecensioni") val numeroRecensioni: Int = 0,
    @SerializedName("organizzatoreId") val organizzatoreId: Long? = null,
    @SerializedName("organizzatoreUsername") val organizzatoreUsername: String? = null,
    @SerializedName("tappe") val tappe: List<AttivitaViaggioDTO> = emptyList()
)