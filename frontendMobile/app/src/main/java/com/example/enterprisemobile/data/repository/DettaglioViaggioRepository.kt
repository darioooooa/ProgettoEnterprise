package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.ViaggioApiService
import com.example.enterprisemobile.data.db.ViaggioDAO
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.model.ViaggioEntity
import okhttp3.ResponseBody
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DettaglioViaggioRepository(
    private val api: ViaggioApiService,
    private val dao: ViaggioDAO
) {
    suspend fun getViaggioLocale(id: Long): ViaggioEntity? = withContext(Dispatchers.IO) {
        dao.getViaggioById(id)
    }

    suspend fun salvaViaggioLocale(viaggio: ViaggioEntity) = withContext(Dispatchers.IO) {
        dao.insertAll(listOf(viaggio))
    }

    suspend fun getViaggioRemoto(id: Long) = withContext(Dispatchers.IO) {
        api.getViaggioById(id)
    }

    suspend fun modificaViaggio(id: Long, payload: ViaggioDTO): Response<ResponseBody> = withContext(Dispatchers.IO) {
        api.modificaViaggio(id, payload)
    }

    suspend fun eliminaViaggio(id: Long): Response<ResponseBody> = withContext(Dispatchers.IO) {
        val response = api.eliminaViaggio(id)
        if (response.isSuccessful) {
            // Rimuove il viaggio dal database Room del telefono
            dao.deleteById(id)
        }
        response
    }
}