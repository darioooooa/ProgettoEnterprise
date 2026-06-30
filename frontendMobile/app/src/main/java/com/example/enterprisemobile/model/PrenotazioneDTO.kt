package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class PrenotazioneDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("dataPrenotazione") val dataPrenotazione: String?,
    @SerializedName("numeroPersone") val numeroPersone: Int,
    @SerializedName("viaggiatoreId") val viaggiatoreId: Long,
    @SerializedName("viaggiatoreUsername") val viaggiatoreUsername: String?,
    @SerializedName("viaggioId") val viaggioId: Long,
    @SerializedName("viaggioTitolo") val viaggioTitolo: String,
    @SerializedName("viaggioDataInizio") val viaggioDataInizio: String?,
    @SerializedName("viaggioDataFine") val viaggioDataFine: String?,
    @SerializedName("viaggioDestinazione") val viaggioDestinazione: String?,
    @SerializedName("stato") val stato: String // "IN_ATTESA", "CONFERMATA", "ANNULLATA"
)