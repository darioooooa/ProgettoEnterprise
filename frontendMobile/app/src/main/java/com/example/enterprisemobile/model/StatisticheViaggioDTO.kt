package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class StatisticheViaggioDTO(
    @SerializedName("mediaRecensioni") val mediaRecensioni: Double,
    @SerializedName("numeroRecensioni") val numeroRecensioni: Int,
    @SerializedName("organizzatoreUsername") val organizzatoreUsername: String?,
    @SerializedName("organizzatoreId") val organizzatoreId: Long?
)