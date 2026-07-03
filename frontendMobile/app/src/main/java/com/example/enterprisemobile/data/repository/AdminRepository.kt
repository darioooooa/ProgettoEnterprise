package com.example.enterprisemobile.data.repository

import android.content.Context
import com.example.enterprisemobile.data.api.AdminApiService
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.db.AppDatabase
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.data.model.RichiestaPromozioneResponse
import com.example.enterprisemobile.model.PageResponse
import okhttp3.ResponseBody
import retrofit2.Response

class AdminRepository(private val context: Context) {

    private val adminApiService: AdminApiService =
        RetrofitClient.ottieniClientBackend(context).create(AdminApiService::class.java)

    private val database = AppDatabase.getInstance(context)
    private val richiestaDao = database.richiestaPromozioneDao()

    suspend fun getRichiestePromozione(
        stato: String? = null,
        username: String? = null,
        page: Int = 0,
        size: Int = 10
    ): PageResponse<RichiestaPromozioneEntity> {
        return try {
            val response = adminApiService.getRichiestePromozione(stato, username, page, size)
            if (response.isSuccessful && response.body() != null) {
                val pageResponse = response.body()!!
                val entities = pageResponse.content.map { mappaDtoAEntity(it) }

                // Aggiorna la cache locale solo se è la prima pagina senza filtri
                if (page == 0 && stato.isNullOrBlank() && username.isNullOrBlank()) {
                    richiestaDao.deleteAll()
                    richiestaDao.insertAll(entities)
                }

                PageResponse(
                    content = entities,
                    totalElements = pageResponse.totalElements,
                    totalPages = pageResponse.totalPages,
                    number = pageResponse.number,
                    size = pageResponse.size
                )
            } else {
                android.util.Log.e("AdminRepo", "API fallita: ${response.code()}")
                PageResponse(emptyList(), 0, 0, 0, size)
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminRepo", "Errore API: ${e.message}")
            val cachedData = richiestaDao.getAllRichieste()
            if (cachedData.isNotEmpty() && page == 0 && stato.isNullOrBlank() && username.isNullOrBlank()) {
                PageResponse(
                    content = cachedData,
                    totalElements = cachedData.size,
                    totalPages = 1,
                    number = 0,
                    size = size
                )
            } else {
                throw e
            }
        }
    }

    suspend fun approvaRichiesta(id: Long, adminId: Long): Response<Unit> {
        return adminApiService.approvaRichiesta(id)
    }

    suspend fun rifiutaRichiesta(id: Long, motivazione: String): Response<Unit> {
        return adminApiService.rifiutaRichiesta(id, mapOf("noteAdmin" to motivazione))
    }

    suspend fun scaricaDocumento(id: Long): Response<ResponseBody> {
        return adminApiService.scaricaDocumento(id)
    }

    private fun mappaDtoAEntity(dto: RichiestaPromozioneResponse): RichiestaPromozioneEntity {
        return RichiestaPromozioneEntity(
            id = dto.id,
            usernameViaggiatore = dto.usernameViaggiatore,
            emailViaggiatore = dto.emailViaggiatore,
            dataRichiesta = dto.dataRichiesta,
            motivazione = dto.motivazione,
            stato = dto.stato,
            biografiaProfessionale = dto.biografiaProfessionale,
            documentiLink = dto.documentiLink,
            adminId = dto.adminId,
            usernameRichiesto = dto.usernameRichiesto,
            emailProfessionale = dto.emailProfessionale
        )
    }
}