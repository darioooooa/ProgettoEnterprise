package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.ViaggioApiService
import com.example.enterprisemobile.data.db.ProgrammaDAO
import com.example.enterprisemobile.model.AttivitaViaggioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProgrammaRepository(
    private val api: ViaggioApiService,
    private val dao: ProgrammaDAO
) {

    suspend fun sincronizzaAttivita(viaggioId: Long, page: Int, filtri: Map<String, String>) = withContext(Dispatchers.IO) {
        try {
            val response = api.getAttivitaViaggio(viaggioId, page, filtri)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!.content ?: emptyList()
                val entities = dtos.map { dto ->
                    AttivitaViaggioEntity(
                        id = dto.id,
                        viaggioId = viaggioId,
                        titolo = dto.titolo,
                        descrizione = dto.descrizione,
                        orarioInizio = dto.orarioInizio,
                        orarioFine = dto.orarioFine,
                        posizione = dto.posizione,
                        costo = dto.costo
                    )
                }
                dao.clearAttivitaViaggio(viaggioId)
                dao.insertAllAttivita(entities)
                response.body()!!.totalPages
            } else 1
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }
    }

    suspend fun getAttivitaLocali(viaggioId: Long): List<AttivitaViaggioEntity> = withContext(Dispatchers.IO) {
        dao.getAttivitaLocali(viaggioId)
    }
}