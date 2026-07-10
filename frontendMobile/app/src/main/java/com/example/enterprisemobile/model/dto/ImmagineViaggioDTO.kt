package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class ImmagineViaggioDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("pubblica") val pubblica: Boolean,
    @SerializedName("viaggioId") val viaggioId: Long
)