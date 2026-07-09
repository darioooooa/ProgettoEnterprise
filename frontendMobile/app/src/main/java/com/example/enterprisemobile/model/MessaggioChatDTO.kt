package com.example.enterprisemobile.data.model

import com.google.gson.annotations.SerializedName

data class MessaggioChatDTO(
    @SerializedName("id")
    val identificativoUnivoco: Long? = null,

    @SerializedName("chatRoomId")
    val identificativoDellaStanza: Long,

    @SerializedName("mittenteUsername")
    val nomeDelMittente: String,

    @SerializedName("testo")
    val contenutoDelMessaggio: String,

    @SerializedName("dataInvio")
    val dataDiSpedizione: Any? = null,

    @SerializedName("mittenteId")
    val identificativoDelMittente: Long
)