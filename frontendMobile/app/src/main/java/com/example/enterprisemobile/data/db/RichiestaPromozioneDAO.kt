package com.example.enterprisemobile.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity

@Dao
interface RichiestaPromozioneDAO {

    @Query("SELECT * FROM richieste_promozione ORDER BY dataRichiesta DESC")
    suspend fun getAllRichieste(): List<RichiestaPromozioneEntity>

    @Query("SELECT * FROM richieste_promozione WHERE stato = :stato ORDER BY dataRichiesta DESC")
    suspend fun getRichiesteByStato(stato: String): List<RichiestaPromozioneEntity>

    @Query("SELECT * FROM richieste_promozione WHERE id = :id")
    suspend fun getRichiestaById(id: Long): RichiestaPromozioneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(richieste: List<RichiestaPromozioneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(richiesta: RichiestaPromozioneEntity)

    @Query("DELETE FROM richieste_promozione")
    suspend fun deleteAll()

    @Query("DELETE FROM richieste_promozione WHERE id = :id")
    suspend fun deleteById(id: Long)
}