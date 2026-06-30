package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.ViaggioApiService
import com.example.enterprisemobile.data.db.CommunityDAO
import com.example.enterprisemobile.model.RecensioneViaggioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityRepository(
    private val api: ViaggioApiService,
    private val dao: CommunityDAO
) {
    suspend fun sincronizzaRecensioni(viaggioId: Long, page: Int, filtri: Map<String, String>): Int = withContext(Dispatchers.IO) {
        try {
            val response = api.getRecensioni(viaggioId, page, filtri)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val dtos = data.content ?: emptyList()
                val entities = dtos.map { dto ->
                    RecensioneViaggioEntity(
                        id = dto.id,
                        viaggioId = viaggioId,
                        voto = dto.voto,
                        commento = dto.commento,
                        utenteUsername = dto.utenteUsername,
                        dataCreazione = dto.dataCreazione
                    )
                }
                dao.clearRecensioniViaggio(viaggioId)
                dao.insertAllRecensioni(entities)
                response.body()!!.totalPages
            }
            else{
                1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }
    }

    suspend fun getRecensioniLocali(viaggioId: Long): List<RecensioneViaggioEntity> = withContext(Dispatchers.IO) {
        dao.getRecensioniLocali(viaggioId)
    }
}