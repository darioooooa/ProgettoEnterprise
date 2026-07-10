package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class UtenteBannatoDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("ruolo") val ruolo: String? = null,
    @SerializedName("dataBan") val dataBan: String? = "N/D",
    @SerializedName("motivoBan") val motivoBan: String? = "Nessun motivo"
)