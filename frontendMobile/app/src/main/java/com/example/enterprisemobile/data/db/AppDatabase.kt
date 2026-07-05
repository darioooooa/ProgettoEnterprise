package com.example.enterprisemobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.enterprisemobile.model.ViaggioEntity
import com.example.enterprisemobile.model.PrenotazioneEntity
import com.example.enterprisemobile.model.AttivitaViaggioEntity
import com.example.enterprisemobile.model.ImmagineViaggioEntity
import com.example.enterprisemobile.model.RecensioneViaggioEntity
import com.example.enterprisemobile.data.model.RichiestaPromozioneEntity
import com.example.enterprisemobile.model.ItinerarioEntity

@Database(
    entities = [
        ViaggioEntity::class,
        PrenotazioneEntity::class,
        AttivitaViaggioEntity::class,
        ImmagineViaggioEntity::class,
        RecensioneViaggioEntity::class,
        RichiestaPromozioneEntity::class,
        ItinerarioEntity::class
    ],
    version = 7,
    exportSchema = false
)

@TypeConverters(ItinerariTypeConverters::class) //Attiva l'uso dei convertitori JSON
abstract class AppDatabase : RoomDatabase() {

    abstract fun viaggioDao(): ViaggioDAO
    abstract fun prenotazioneDao(): PrenotazioneDAO
    abstract fun galleriaDao(): GalleriaDAO
    abstract fun dettaglioViaggioDao(): DettaglioViaggioDAO
    abstract fun programmaDao(): ProgrammaDAO
    abstract fun communityDao(): CommunityDAO
    abstract fun richiestaPromozioneDao(): RichiestaPromozioneDAO
    abstract fun itinerarioDao(): ItinerarioDAO

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "enterprise-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}