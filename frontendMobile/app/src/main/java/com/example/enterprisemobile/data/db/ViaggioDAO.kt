package com.example.enterprisemobile.data.db

import androidx.room.*
import com.example.enterprisemobile.model.ViaggioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ViaggioDAO {
    @Query("SELECT * FROM viaggi ORDER BY dataInizio ASC")
    fun getAllViaggi(): Flow<List<ViaggioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(viaggi: List<ViaggioEntity>)

    @Query("SELECT * FROM viaggi WHERE id = :id")
    suspend fun getViaggioById(id: Long): ViaggioEntity?

    @Query("DELETE FROM viaggi")
    fun deleteAll()
}