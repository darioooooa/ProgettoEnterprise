package com.example.enterprisemobile.model

import com.google.gson.annotations.SerializedName

data class PageResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    @SerializedName("totalElements")
    val totalElements: Int = 0,
    @SerializedName("totalPages")
    val totalPages: Int = 0,
    @SerializedName("number")
    val number: Int = 0,
    @SerializedName("size")
    val size: Int = 10,
    @SerializedName("first")
    val first: Boolean = false,
    @SerializedName("last")
    val last: Boolean = false
)