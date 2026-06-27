package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.api.ViaggioApiService
import com.example.enterprisemobile.data.db.ViaggioDAO
import com.example.enterprisemobile.model.ViaggioEntity
import com.example.enterprisemobile.model.ViaggioMappaDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ViaggioRepository(
    private val api: ViaggioApiService,
    private val dao: ViaggioDAO
) {
    val allViaggi: Flow<List<ViaggioEntity>> = dao.getAllViaggi()

    suspend fun refreshViaggiFiltrati(dest: String, dMin: String, dMax: String, post: String, pMin: String, pMax: String, pag: Int) {
        // Chiamata all'API con i filtri
        val response = api.getViaggiFiltrati(dest, dMin, dMax, post, pMin, pMax, pag)

        val viaggiEntity = response.content.map { dto ->
            ViaggioEntity(
                id = dto.id ?: 0L,
                titolo = dto.titolo,
                destinazione = dto.destinazione,
                cittaPartenza = dto.cittaPartenza,
                prezzo = dto.prezzo,
                dataInizio = dto.dataInizio,
                dataFine = dto.dataFine
            )
        }

        withContext(Dispatchers.IO) {
            dao.deleteAll()
            dao.insertAll(viaggiEntity)
        }
    }
    suspend fun getViaggiMappa(): List<ViaggioMappaDTO> {
        return try {
            api.getViaggiPerMappa()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}