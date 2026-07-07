package com.example.enterprisemobile.data.api

import com.example.enterprisemobile.model.InvitoSospesoDTO
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ItinerariApiService {

    // Crea nuovo itinerario
    @POST("itinerari-preferiti")
    suspend fun creaNuovoItinerario(
        @Body nuovoItinerario: ItinerarioPreferitoDTO
    ): Response<ItinerarioPreferitoDTO>

    // Recupera le liste personali
    @GET("itinerari-preferiti/mie-liste")
    suspend fun ottieniMieListe(): Response<List<ItinerarioPreferitoDTO>>

    // Recupera le liste condivise
    @GET("itinerari-preferiti/condivisi")
    suspend fun getItinerariCondivisiConMe(): Response<List<ItinerarioPreferitoDTO>>

    // Ricerca le liste pubbliche per nome
    @GET("itinerari-preferiti/ricerca-pubblica")
    suspend fun cercaListePubbliche(
        @Query("nome") nome: String
    ): Response<List<ItinerarioPreferitoDTO>>

    // Recupera il dettaglio di un singolo itinerario specifico tramite id
    @GET("itinerari-preferiti/{id}")
    suspend fun getDettaglioLista(
        @Path("id") id: Long
    ): Response<ItinerarioPreferitoDTO>

    // Cambia la visibilità (PRIVATA / PUBBLICA)
    @PATCH("itinerari-preferiti/{id}/visibilita")
    suspend fun cambiaVisibilita(
        @Path("id") id: Long,
        @Query("nuovaVisibilita") nuovaVisibilita: String
    ): Response<ItinerarioPreferitoDTO>

    // Elimina un itinerario dal server
    @DELETE("itinerari-preferiti/{id}")
    suspend fun cancellaItinerarioDalServer(
        @Path("id") id: Long
    ): Response<ResponseBody>

    // Aggiunge un viaggio specifico a un itinerario preferito
    @POST("itinerari-preferiti/{idLista}/viaggi/{idViaggio}")
    suspend fun aggiungiViaggioAItinerario(
        @Path("idLista") idLista: Long,
        @Path("idViaggio") idViaggio: Long
    ): Response<ResponseBody>

    // Rimuove un viaggio dall'itinerario
    @DELETE("itinerari-preferiti/{idLista}/viaggi/{idViaggio}")
    suspend fun rimuoviViaggioDaItinerario(
        @Path("idLista") idLista: Long,
        @Path("idViaggio") idViaggio: Long
    ): Response<ResponseBody>

    // Sposta un viaggio da un itinerario all'altro
    @POST("itinerari-preferiti/{idSorgente}/sposta-in/{idDestinazione}/viaggi/{idViaggio}")
    suspend fun spostaViaggioTraItinerari(
        @Path("idSorgente") idSorgente: Long,
        @Path("idDestinazione") idDestinazione: Long,
        @Path("idViaggio") idViaggio: Long
    ): Response<ResponseBody>

    // Recupera le liste pubbliche create da un utente specifico cercando per username
    @GET("itinerari-preferiti/utente/{username}/pubblici")
    suspend fun getListePubblicheUtente(
        @Path("username") username: String
    ): Response<List<ItinerarioPreferitoDTO>>

    // Invita un amico a collaborare tramite email
    @POST("itinerari-preferiti/{id}/invita")
    suspend fun invitaCollaboratore(
        @Path("id") idItinerario: Long,
        @Body payload: Map<String, String>
    ): Response<ResponseBody>

    // Recupera le richieste di condivisione in sospeso
    @GET("itinerari-preferiti/inviti")
    suspend fun getInvitiInSospeso(): Response<List<InvitoSospesoDTO>>

    // Accetta un invito di condivisione
    @POST("itinerari-preferiti/{id}/accetta-invito")
    suspend fun accettaInvito(
        @Path("id") id: Long
    ): Response<Unit>

    // Rifiuta un invito di condivisione
    @POST("itinerari-preferiti/{id}/rifiuta-invito")
    suspend fun rifiutaInvito(
        @Path("id") id: Long
    ): Response<Unit>
}