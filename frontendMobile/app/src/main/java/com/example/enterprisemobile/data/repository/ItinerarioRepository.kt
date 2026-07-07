package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.ItinerariApiService
import com.example.enterprisemobile.data.db.ItinerarioDAO
import com.example.enterprisemobile.model.InvitoSospesoDTO
import com.example.enterprisemobile.model.ItinerarioEntity
import com.example.enterprisemobile.model.ItinerarioPreferitoDTO
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.model.Visibilita
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

class ItinerarioRepository(
    private val api: ItinerariApiService,
    private val dao: ItinerarioDAO
) {
    private val gson = Gson()

    // Carica i miei itinerari (con fallback offline)
    suspend fun getMieiItinerari(): List<ItinerarioPreferitoDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.ottieniMieListe()
            if (response.isSuccessful && response.body() != null) {
                val remoti = response.body()!!
                val entities = remoti.mapIndexed { index, dto ->
                    // Se l'id è nullo, si crea un id temporaneo univoco basato sull'hash del nome e dell'indice
                    val idUnivocoLocale = dto.idItinerario ?: (dto.nome.hashCode().toLong() + index)

                    ItinerarioEntity(
                        idItinerario = idUnivocoLocale,
                        nome = dto.nome,
                        inCondivisione = dto.inCondivisione ?: false,
                        proprietarioUsername = dto.proprietarioUsername ?: "Io",
                        dataCreazioneStr = dto.dataCreazione ?: "",
                        visibilita = dto.visibilita.name,
                        viaggiContenutiJson = gson.toJson(dto.viaggiContenuti ?: emptyList<ViaggioDTO>()),
                        isCondivisoConMe = false
                    )
                }
                dao.svuotaTabella(condivisi = false)
                dao.insertAll(entities)
                return@withContext remoti
            }
        } catch (e: Exception) {
            android.util.Log.e("ItinerarioRepo", "Errore di rete per i miei itinerari, uso cache: ${e.message}")
        }
        return@withContext dao.getMieiItinerariLocali().map { convertiEntityADto(it) }
    }

    // Carica itinerari condivisi (con fallback offline)
    suspend fun getItinerariCondivisi(): List<ItinerarioPreferitoDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.getItinerariCondivisiConMe()
            if (response.isSuccessful && response.body() != null) {
                val remoti = response.body()!!
                val entities = remoti.mapIndexed { index, dto ->
                    val idUnivocoLocale = dto.idItinerario ?: (dto.nome.hashCode().toLong() + index + 5000) // offset per non collidere con i personali

                    ItinerarioEntity(
                        idItinerario = idUnivocoLocale,
                        nome = dto.nome,
                        inCondivisione = true,
                        proprietarioUsername = dto.proprietarioUsername ?: "Amico",
                        dataCreazioneStr = dto.dataCreazione ?: "",
                        visibilita = dto.visibilita.name,
                        viaggiContenutiJson = gson.toJson(dto.viaggiContenuti ?: emptyList<ViaggioDTO>()),
                        isCondivisoConMe = true
                    )
                }
                dao.svuotaTabella(condivisi = true)
                dao.insertAll(entities)
                return@withContext remoti
            }
        } catch (e: Exception) {
            android.util.Log.e("ItinerarioRepo", "Errore di rete per itinerari condivisi, uso cache: ${e.message}")
        }
        return@withContext dao.getItinerariCondivisiLocali().map { convertiEntityADto(it) }
    }

    // Recupera richieste di condivisione in sospeso
    suspend fun getInvitiInSospeso(): List<InvitoSospesoDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.getInvitiInSospeso()
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (e: Exception) {
            android.util.Log.e("ItinerarioRepo", "Errore recupero richieste pendenti: ${e.message}")
        }
        return@withContext emptyList()
    }

    // Crea nuovo itinerario
    suspend fun creaItinerario(nome: String, visibilita: Visibilita): Response<ItinerarioPreferitoDTO> = withContext(Dispatchers.IO) {
        val nuovoDto = ItinerarioPreferitoDTO(nome = nome, visibilita = visibilita)
        api.creaNuovoItinerario(nuovoDto)
    }

    // Recupera il dettaglio di un singolo itinerario specifico dal server
    suspend fun getDettaglioLista(id: Long): Response<ItinerarioPreferitoDTO> = withContext(Dispatchers.IO) {
        api.getDettaglioLista(id)
    }

    // Cambia la visibilità di una lista
    suspend fun cambiaVisibilita(id: Long, visibilitaAttuale: Visibilita): Response<ItinerarioPreferitoDTO> = withContext(Dispatchers.IO) {
        val prossimaVisibilita = if (visibilitaAttuale == Visibilita.PRIVATA) "PUBBLICA" else "PRIVATA"
        api.cambiaVisibilita(id, prossimaVisibilita)
    }

    // Elimina l'intero itinerario
    suspend fun eliminaItinerario(id: Long): Response<ResponseBody> = withContext(Dispatchers.IO) {
        val response = api.cancellaItinerarioDalServer(id)
        if (response.isSuccessful) {
            dao.eliminaInLocale(id)
        }
        response
    }

    // Aggiunge un viaggio specifico a un itinerario preferito sul server
    suspend fun aggiungiViaggioAItinerario(idLista: Long, idViaggio: Long): Response<ResponseBody> = withContext(Dispatchers.IO) {
        api.aggiungiViaggioAItinerario(idLista, idViaggio)
    }

    // Rimuove un singolo viaggio da un itinerario
    suspend fun rimuoviViaggioDaItinerario(idLista: Long, idViaggio: Long): Response<ResponseBody> = withContext(Dispatchers.IO) {
        api.rimuoviViaggioDaItinerario(idLista, idViaggio)
    }

    // Sposta un viaggio tra due itinerari
    suspend fun spostaViaggioTraItinerari(idSorgente: Long, idDestinazione: Long, idViaggio: Long): Response<ResponseBody> = withContext(Dispatchers.IO) {
        api.spostaViaggioTraItinerari(idSorgente, idDestinazione, idViaggio)
    }

    // Recupera le liste pubbliche create da un utente cercando per username
    suspend fun getListePubblicheUtente(username: String): Response<List<ItinerarioPreferitoDTO>> = withContext(Dispatchers.IO) {
        api.getListePubblicheUtente(username)
    }

    // Invita un collaboratore tramite email
    suspend fun invitaCollaboratore(idItinerario: Long, email: String): Response<ResponseBody> = withContext(Dispatchers.IO) {
        api.invitaCollaboratore(idItinerario, mapOf("email" to email))
    }

    // Accetta richiesta condivisione
    suspend fun accettaInvito(id: Long): Response<Unit> = withContext(Dispatchers.IO) {
        api.accettaInvito(id)
    }

    // Rifiuta richiesta condivisione
    suspend fun rifiutaInvito(id: Long): Response<Unit> = withContext(Dispatchers.IO) {
        api.rifiutaInvito(id)
    }

    // Conversione interna da Entity (Room) a DTO (UI)
    private fun convertiEntityADto(entity: ItinerarioEntity): ItinerarioPreferitoDTO {
        val listType = object : TypeToken<List<ViaggioDTO>>() {}.type
        val viaggi: List<ViaggioDTO> = gson.fromJson(entity.viaggiContenutiJson, listType)
        return ItinerarioPreferitoDTO(
            idItinerario = entity.idItinerario,
            nome = entity.nome,
            inCondivisione = entity.inCondivisione,
            proprietarioUsername = entity.proprietarioUsername ?: "Anonimo",
            dataCreazione = entity.dataCreazioneStr ?: "",
            visibilita = Visibilita.valueOf(entity.visibilita),
            viaggiContenuti = viaggi
        )
    }
}