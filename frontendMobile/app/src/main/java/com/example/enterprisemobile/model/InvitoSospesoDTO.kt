package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class InvitoSospesoDTO(
    @SerializedName("idItinerario") val idItinerario: Long,
    @SerializedName("nomeItinerario") val nomeItinerario: String,
    @SerializedName("proprietario") val proprietario: String,
    @SerializedName("emailProprietario") val emailProprietario: String
)