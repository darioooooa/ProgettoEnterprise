package com.example.enterprisemobile.data.db

import androidx.room.*
import com.example.enterprisemobile.model.AttivitaViaggioEntity

@Dao
interface ProgrammaDAO {

    @Query("SELECT * FROM attivita_viaggio WHERE viaggioId = :viaggioId ORDER BY orarioInizio ASC")
    suspend fun getAttivitaLocali(viaggioId: Long): List<AttivitaViaggioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttivita(attivita: List<AttivitaViaggioEntity>)

    @Query("DELETE FROM attivita_viaggio WHERE viaggioId = :viaggioId")
    suspend fun clearAttivitaViaggio(viaggioId: Long)
}