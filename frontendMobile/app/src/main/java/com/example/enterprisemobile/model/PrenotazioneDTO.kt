package com.example.enterprisemobile.data.model

import com.google.gson.annotations.SerializedName

data class PrenotazioneDTO(
    val id: Long,
    val dataPrenotazione: String?,
    val numeroPersone: Int,
    val viaggiatoreId: Long,
    val viaggiatoreUsername: String?,
    val viaggioId: Long,
    val viaggioTitolo: String,
    val viaggioDataInizio: String?,
    val viaggioDataFine: String?,
    val viaggioDestinazione: String?,
    val stato: String // "IN_ATTESA", "CONFERMATA", "ANNULLATA"
)