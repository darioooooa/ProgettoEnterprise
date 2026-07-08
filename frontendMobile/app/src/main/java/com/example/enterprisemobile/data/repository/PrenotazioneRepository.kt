package com.example.enterprisemobile.data.repository
import com.example.enterprisemobile.data.api.PrenotazioneApiService
import com.example.enterprisemobile.data.api.PrenotazioneRequest
import com.example.enterprisemobile.data.api.PrenotazioneResponse
import com.example.enterprisemobile.data.db.PrenotazioneDAO
import com.example.enterprisemobile.model.PrenotazioneDTO
import com.example.enterprisemobile.model.PrenotazioneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class PrenotazioneRepository(
    private val api: PrenotazioneApiService,
    private val dao: PrenotazioneDAO
) {

    suspend fun creaNuovaPrenotazione(viaggioId: Long, numPersone: Int): PrenotazioneResponse {
        return withContext(Dispatchers.IO) {
            val richiesta = PrenotazioneRequest(numPersone)
            api.creaPrenotazione(viaggioId, richiesta)
        }
    }
    suspend fun caricaMiePrenotazioniDaServer(page: Int = 0) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMiePrenotazioni(page)
                if (response.isSuccessful) {
                    val dtos = response.body()?.content ?: emptyList()
                    val entities = dtos.map { dto ->
                        PrenotazioneEntity(
                            id = dto.id,
                            dataPrenotazione = dto.dataPrenotazione,
                            numeroPersone = dto.numeroPersone,
                            viaggioId = dto.viaggioId,
                            viaggioTitolo = dto.viaggioTitolo,
                            viaggioDestinazione = dto.viaggioDestinazione,
                            viaggioDataInizio = dto.viaggioDataInizio,
                            viaggioDataFine = dto.viaggioDataFine,
                            stato = dto.stato
                        )
                    }
                    if (page == 0) {
                        dao.clearAll()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    suspend fun getMiePrenotazioniLocali(): List<PrenotazioneEntity> {
        return withContext(Dispatchers.IO) {
            dao.getAllPrenotazioni()
        }
    }

    // QUESTO SERVE ALL'ORGANIZZATORE
    suspend fun getPrenotazioniPerImeiViaggi(page: Int,stato: String?=null, usernameViaggiatore: String? = null): com.example.enterprisemobile.model.PageResponse<PrenotazioneDTO>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMiePrenotazioni(
                    page = page,
                    stato = stato,
                    usernameViaggiatore = usernameViaggiatore
                )

                if (response.isSuccessful) {
                    // Restituiamo tutto il blocco intero (dati + info sulle pagine)
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}