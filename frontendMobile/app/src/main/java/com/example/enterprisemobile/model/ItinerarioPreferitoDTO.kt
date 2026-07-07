package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class ItinerarioPreferitoDTO(
    @SerializedName("idItinerario") val idItinerario: Long? = null,
    @SerializedName("nome") val nome: String,
    @SerializedName("inCondivisione") val inCondivisione: Boolean? = null,
    @SerializedName("proprietarioUsername") val proprietarioUsername: String? = null,
    @SerializedName("dataCreazione") val dataCreazione: String? = null,
    @SerializedName("visibilita") val visibilita: Visibilita,
    @SerializedName("viaggiContenuti") val viaggiContenuti: List<ViaggioDTO>? = null
)