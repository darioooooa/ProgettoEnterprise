package com.example.enterprisemobile.data.repository

import com.example.enterprisemobile.data.api.ViaggioApiService
import com.example.enterprisemobile.data.db.ViaggioDAO
import com.example.enterprisemobile.model.ViaggioDTO
import com.example.enterprisemobile.model.ViaggioEntity
import com.example.enterprisemobile.model.ViaggioMappaDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.enterprisemobile.model.CreaViaggioDTO

class ViaggioRepository(
    private val api: ViaggioApiService,
    private val dao: ViaggioDAO
) {
    val allViaggi: Flow<List<ViaggioEntity>> = dao.getAllViaggi()

    suspend fun refreshViaggiFiltrati(dest: String, dMin: String, dMax: String, post: String, pMin: String, pMax: String, pag: Int): Int {
        val response = api.getViaggiFiltrati(dest, dMin, dMax, post, pMin, pMax, pag)

        val viaggiEntity = response.content.map { dto ->
            ViaggioEntity(
                id = dto.id ?: 0L,
                titolo = dto.titolo,
                destinazione = dto.destinazione,
                cittaPartenza = dto.cittaPartenza,
                prezzo = dto.prezzo,
                dataInizio = dto.dataInizio,
                dataFine = dto.dataFine,
                descrizione = dto.descrizione,
                maxPartecipanti = dto.maxPartecipanti,
                partecipantiAttuali = dto.partecipantiAttuali,
                organizzatoreId = null
            )
        }

        withContext(Dispatchers.IO) {
            dao.deleteAll()
            dao.insertAll(viaggiEntity)
        }
        return response.totalPages
    }

    // Sincronizza i dati dell'organizzatore e fa da paracadute se il server è irraggiungibile
    suspend fun getViaggiOrganizzatoreConFallback(organizzatoreId: Long): List<ViaggioDTO> {
        return try {
            // Prova a scaricare i viaggi dal server Spring Boot
            val risposta = api.getViaggiByOrganizzatore(organizzatoreId)
            if (risposta.isSuccessful && risposta.body() != null) {
                val viaggiDto = risposta.body()!!

                // Salva i viaggi scaricati dentro Room per le prossime sessioni offline
                val entities = viaggiDto.map { dto ->
                    ViaggioEntity(
                        id = dto.id ?: 0L,
                        titolo = dto.titolo,
                        destinazione = dto.destinazione,
                        cittaPartenza = dto.cittaPartenza,
                        prezzo = dto.prezzo,
                        dataInizio = dto.dataInizio,
                        dataFine = dto.dataFine,
                        descrizione = dto.descrizione,
                        maxPartecipanti = dto.maxPartecipanti,
                        partecipantiAttuali = dto.partecipantiAttuali,
                        organizzatoreId = organizzatoreId
                    )
                }
                withContext(Dispatchers.IO) {
                    dao.insertAll(entities)
                }
                viaggiDto
            } else {
                // Fallback: se la risposta del server non è ottimale, carica la cache locale
                recuperaViaggiLocali(organizzatoreId)
            }
        } catch (e: Exception) {
            // C'è un problema di rete o eccezione, carica i dati da Room
            e.printStackTrace()
            recuperaViaggiLocali(organizzatoreId)
        }
    }

    // Funzione per convertire le entity locali in DTO
    private suspend fun recuperaViaggiLocali(organizzatoreId: Long): List<ViaggioDTO> = withContext(Dispatchers.IO) {
        val entitaLocali = dao.getViaggiByOrganizzatoreLocale(organizzatoreId)
        entitaLocali.map { entity ->
            ViaggioDTO(
                id = entity.id,
                titolo = entity.titolo,
                descrizione = entity.descrizione,
                stato = "ATTIVO", // Valore di default offline
                maxPartecipanti = entity.maxPartecipanti,
                partecipantiAttuali = entity.partecipantiAttuali,
                destinazione = entity.destinazione,
                cittaPartenza = entity.cittaPartenza,
                prezzo = entity.prezzo,
                dataInizio = entity.dataInizio,
                dataFine = entity.dataFine,
                latitudine = 0.0,
                longitudine = 0.0,
                mediaRecensioni = 0.0 // Offline imposta a zero se non salvato
            )
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

    suspend fun creaViaggio(viaggio: CreaViaggioDTO): Result<ViaggioDTO> {
        return try {
            val response = api.creaViaggio(viaggio)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Errore dal server: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}