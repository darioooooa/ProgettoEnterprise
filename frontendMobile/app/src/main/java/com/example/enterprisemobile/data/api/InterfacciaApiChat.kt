package com.example.enterprisemobile.data.api
import retrofit2.http.Query
import com.example.enterprisemobile.data.model.MessaggioChatDTO
import com.example.enterprisemobile.data.model.StanzaChatDTO
import retrofit2.http.GET
import retrofit2.http.POST
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

    @retrofit2.http.GET("/api/chat/organizzatore")
    suspend fun caricaStanzeOrganizzatore(
        @retrofit2.http.Query("organizzatoreUsername") nomeUtente: String
    ): List<StanzaChatDTO>


    @POST("/api/v1/segnalazioni/crea")
    suspend fun creaSegnalazione(
        @retrofit2.http.Query("idSegnalatore") idUtenteSegnalatore: Long,
        @retrofit2.http.Body richiesta: com.example.enterprisemobile.data.model.RichiestaSegnalazioneDTO
    ): retrofit2.Response<Unit>



}