package com.example.enterprisemobile.data.model

import com.google.gson.annotations.SerializedName

data class RichiestaSegnalazioneDTO(
    @SerializedName("tipo")
    val tipoEntita: String,

    @SerializedName("idRiferimento")
    val identificativoDiRiferimento: Long,

    @SerializedName("motivo")
    val motivazioneSelezionata: String,

    @SerializedName("descrizione")
    val descrizioneDettagliata: String
)