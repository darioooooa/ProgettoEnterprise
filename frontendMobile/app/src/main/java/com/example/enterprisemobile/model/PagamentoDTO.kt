package com.example.enterprisemobile.model

data class PagamentoDTO(
    val idPrenotazione: Long,
    val importo: Double,
    val ricevutaPagamento: String,
    val titolareCarta: String,
    val statoPagamento: String? = null
)