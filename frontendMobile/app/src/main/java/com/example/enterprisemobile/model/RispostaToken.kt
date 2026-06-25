package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

// Modello di dati (DTO) che mappa la risposta standard OAuth2/OpenID Connect di Keycloak.
// Utilizza @SerializedName per agganciare le chiavi esatte del JSON alle variabili Kotlin.
data class RispostaToken(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Long?,
    @SerializedName("token_type") val tokenType: String
)