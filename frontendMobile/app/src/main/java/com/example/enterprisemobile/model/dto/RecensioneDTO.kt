package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class RecensioneDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("voto") val voto: Int,
    @SerializedName("commento") val commento: String?,
    @SerializedName("utenteUsername") val utenteUsername: String?,
    @SerializedName("dataCreazione") val dataCreazione: String?
)