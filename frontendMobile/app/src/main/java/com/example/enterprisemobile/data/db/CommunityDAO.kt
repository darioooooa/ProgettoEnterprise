package com.example.enterprisemobile.data.db

import androidx.room.*
import com.example.enterprisemobile.model.RecensioneViaggioEntity

@Dao
interface CommunityDAO {
    @Query("SELECT * FROM recensioni_viaggio WHERE viaggioId = :viaggioId ORDER BY dataCreazione DESC")
    suspend fun getRecensioniLocali(viaggioId: Long): List<RecensioneViaggioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecensioni(recensioni: List<RecensioneViaggioEntity>)

    @Query("DELETE FROM recensioni_viaggio WHERE viaggioId = :viaggioId")
    suspend fun clearRecensioniViaggio(viaggioId: Long)
}