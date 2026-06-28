package com.example.enterprisemobile.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.enterprisemobile.model.PrenotazioneEntity

@Dao
interface PrenotazioneDAO {
    @Query("SELECT * FROM prenotazioni ORDER BY dataPrenotazione DESC")
    suspend fun getAllPrenotazioni(): List<PrenotazioneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prenotazioni: List<PrenotazioneEntity>)

    @Query("DELETE FROM prenotazioni")
    suspend fun clearAll()
}