package com.example.enterprisemobile.data.api
import retrofit2.http.Query
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import com.example.enterprisemobile.data.model.StanzaChatDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface InterfacciaApiChat {
    @GET("/api/chat/stanza/{identificativoStanza}/cronologia")
    suspend fun ottieniCronologiaStorica(
        @Path("identificativoStanza") identificativoStanza: Long
    ): List<MessaggioChatDTO>

    @GET("/api/chat/viaggiatore")
    suspend fun caricaLeMieStanze(
        @Query("viaggiatoreUsername") nomeUtente: String
    ): List<StanzaChatDTO>

    @retrofit2.http.PATCH("/api/chat/{identificativoStanza}/leggi")
    suspend fun segnaMessaggiComeLetti(
        @retrofit2.http.Path("identificativoStanza") identificativoStanza: Long,
        @retrofit2.http.Query("username") nomeUtente: String
    )


}