package com.example.enterprisemobile.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.enterprisemobile.model.ImmagineViaggioEntity

@Dao
interface GalleriaDAO {

    @Query("SELECT * FROM immagini_viaggio WHERE viaggioId = :viaggioId")
    suspend fun getImmaginiLocali(viaggioId: Long): List<ImmagineViaggioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllImmagini(immagini: List<ImmagineViaggioEntity>)

    @Query("DELETE FROM immagini_viaggio WHERE viaggioId = :viaggioId")
    suspend fun clearImmaginiViaggio(viaggioId: Long)
}