package com.example.enterprisemobile.model

import java.time.LocalDate


data class ItinerarioPreferitoDTO(
    val idItinerario: Long? = null,
    val nome: String,
    val inCondivisione: Boolean? = null,
    val proprietarioUsername: String? = null,
    val dataCreazione: LocalDate? = null,
    val visibilita: Visibilita,
    val viaggiContenuti: List<ViaggioDTO>? = null
)