package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.ViaggioApiService
import com.example.enterprisemobile.data.db.GalleriaDAO
import com.example.enterprisemobile.model.ImmagineViaggioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleriaRepository(
    private val api: ViaggioApiService,
    private val dao: GalleriaDAO
) {

    // Sincronizza i dati remoti scaricandoli dentro Room
    suspend fun sincronizzaGalleria(viaggioId: Long) = withContext(Dispatchers.IO) {
        try {
            val response = api.getGalleria(viaggioId)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!

                // Mappa solo se l'id del backend è valido
                val entities = dtos.map { dto ->
                    ImmagineViaggioEntity(
                        id = dto.id,
                        viaggioId = viaggioId,
                        url = dto.url,
                        pubblica = dto.pubblica
                    )
                }

                dao.clearImmaginiViaggio(viaggioId)
                dao.insertAllImmagini(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getImmaginiLocali(viaggioId: Long): List<ImmagineViaggioEntity> = withContext(Dispatchers.IO) {
        dao.getImmaginiLocali(viaggioId)
    }
}