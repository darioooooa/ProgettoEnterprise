package com.example.enterprisemobile.model

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Int = 0,
    val totalPages: Int = 0
)