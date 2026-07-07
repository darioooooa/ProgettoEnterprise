package com.example.enterprisemobile.data.db

import androidx.room.*
import com.example.enterprisemobile.model.ItinerarioEntity

@Dao
interface ItinerarioDAO {

    // Recupera i miei itinerari personali (ordinati dal più recente)
    @Query("SELECT * FROM itinerari WHERE isCondivisoConMe = 0 ORDER BY idItinerario DESC")
    suspend fun getMieiItinerariLocali(): List<ItinerarioEntity>

    // Recupera gli itinerari che gli amici hanno condiviso con me
    @Query("SELECT * FROM itinerari WHERE isCondivisoConMe = 1 ORDER BY idItinerario DESC")
    suspend fun getItinerariCondivisiLocali(): List<ItinerarioEntity>

    // Recupera un singolo itinerario tramite il suo id (utile per i dettagli offline)
    @Query("SELECT * FROM itinerari WHERE idItinerario = :id")
    suspend fun getItinerarioById(id: Long): ItinerarioEntity?

    // Inserisce o aggiorna gli itinerari (sovrascrive se l'id è già esistente)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itinerari: List<ItinerarioEntity>)

    // Elimina un singolo itinerario specifico dalla cache locale
    @Query("DELETE FROM itinerari WHERE idItinerario = :id")
    suspend fun eliminaInLocale(id: Long)

    // Svuota la tabella in modo selettivo (cancella solo i miei o solo i condivisi)
    @Query("DELETE FROM itinerari WHERE isCondivisoConMe = :condivisi")
    suspend fun svuotaTabella(condivisi: Boolean)
}