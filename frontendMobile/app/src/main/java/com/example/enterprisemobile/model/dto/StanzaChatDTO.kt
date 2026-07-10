package com.example.enterprisemobile.data.model

import com.google.gson.annotations.SerializedName

data class StanzaChatDTO(
    @SerializedName("id")
    val identificativoStanza: Long,

    @SerializedName("viaggioId")
    val identificativoDelViaggio: Long,

    @SerializedName("titoloViaggio")
    val titoloDelViaggio: String,

    @SerializedName("viaggiatoreUsername")
    val nomeUtenteViaggiatore: String,

    @SerializedName("organizzatoreUsername")
    val nomeUtenteOrganizzatore: String,

    @SerializedName("messaggiNonLetti")
    val numeroMessaggiNonLetti: Int,

    @SerializedName("dataUltimoMessaggio")
    val dataDiSpedizioneUltimoMessaggio: String? = null
)