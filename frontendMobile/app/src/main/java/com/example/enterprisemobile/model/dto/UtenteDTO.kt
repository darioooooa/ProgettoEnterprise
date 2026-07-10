package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class UtenteDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("cognome") val cognome: String,
    @SerializedName("nomeCompleto") val nomeCompleto: String?,
    @SerializedName("ruolo") val ruolo: String?
)