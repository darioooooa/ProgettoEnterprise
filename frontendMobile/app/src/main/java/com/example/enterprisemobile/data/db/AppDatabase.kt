package com.example.enterprisemobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.enterprisemobile.model.ViaggioEntity
import com.example.enterprisemobile.model.PrenotazioneEntity


@Database(entities = [ViaggioEntity::class, PrenotazioneEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun viaggioDao(): ViaggioDAO
    abstract fun prenotazioneDao(): PrenotazioneDAO


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